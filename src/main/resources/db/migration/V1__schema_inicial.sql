-- Tabelas TB_ da API Java do Remembered. Todo registro de TB_USUARIO e um idoso.

CREATE TABLE TB_USUARIO (
    id                 NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome               VARCHAR2(120) NOT NULL,
    email              VARCHAR2(160) NOT NULL,
    senha_hash         VARCHAR2(100) NOT NULL,
    push_token         VARCHAR2(200),
    codigo_pareamento  VARCHAR2(10),
    codigo_expira_em   TIMESTAMP,
    criado_em          TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT uk_usuario_email  UNIQUE (email),
    CONSTRAINT uk_usuario_codigo UNIQUE (codigo_pareamento)
);

CREATE TABLE TB_MEDICAMENTO (
    id               NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    idoso_id         NUMBER NOT NULL,
    nome             VARCHAR2(120) NOT NULL,
    dosagem          VARCHAR2(60),
    forma            VARCHAR2(40),
    cor_icone        VARCHAR2(20),
    formato_icone    VARCHAR2(20),
    critico          CHAR(1) DEFAULT 'N' NOT NULL,
    limite_pulos     NUMBER(3) DEFAULT 3 NOT NULL,
    data_inicio      DATE NOT NULL,
    data_fim         DATE,
    ativo            CHAR(1) DEFAULT 'S' NOT NULL,
    observacoes      VARCHAR2(500),
    criado_por_tipo  VARCHAR2(10) NOT NULL,
    criado_por_nome  VARCHAR2(120) NOT NULL,
    CONSTRAINT fk_medicamento_idoso FOREIGN KEY (idoso_id) REFERENCES TB_USUARIO (id),
    CONSTRAINT ck_medicamento_critico CHECK (critico IN ('S', 'N')),
    CONSTRAINT ck_medicamento_ativo   CHECK (ativo IN ('S', 'N')),
    CONSTRAINT ck_medicamento_limite  CHECK (limite_pulos >= 1),
    CONSTRAINT ck_medicamento_criador CHECK (criado_por_tipo IN ('IDOSO', 'CUIDADOR', 'CHAT'))
);

CREATE TABLE TB_HORARIO_MEDICAMENTO (
    id              NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    medicamento_id  NUMBER NOT NULL,
    hora            VARCHAR2(5) NOT NULL,
    dias_semana     VARCHAR2(7) DEFAULT '1111111' NOT NULL,
    CONSTRAINT fk_horario_medicamento FOREIGN KEY (medicamento_id) REFERENCES TB_MEDICAMENTO (id) ON DELETE CASCADE,
    CONSTRAINT ck_horario_hora CHECK (REGEXP_LIKE(hora, '^([01][0-9]|2[0-3]):[0-5][0-9]$')),
    CONSTRAINT ck_horario_dias CHECK (REGEXP_LIKE(dias_semana, '^[01]{7}$'))
);

CREATE TABLE TB_REGISTRO_DOSE (
    id                NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    medicamento_id    NUMBER NOT NULL,
    horario_previsto  TIMESTAMP NOT NULL,
    status            VARCHAR2(10) DEFAULT 'PENDENTE' NOT NULL,
    confirmada_em     TIMESTAMP,
    origem            VARCHAR2(10),
    CONSTRAINT fk_dose_medicamento FOREIGN KEY (medicamento_id) REFERENCES TB_MEDICAMENTO (id) ON DELETE CASCADE,
    CONSTRAINT uk_dose_medicamento_horario UNIQUE (medicamento_id, horario_previsto),
    CONSTRAINT ck_dose_status CHECK (status IN ('PENDENTE', 'TOMADA', 'PULADA')),
    CONSTRAINT ck_dose_origem CHECK (origem IS NULL OR origem IN ('APP', 'CHAT', 'CUIDADOR'))
);

CREATE TABLE TB_AVISO (
    id               NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    idoso_id         NUMBER NOT NULL,
    tipo             VARCHAR2(12) NOT NULL,
    referencia_id    NUMBER,
    titulo           VARCHAR2(120) NOT NULL,
    resumo           VARCHAR2(300) NOT NULL,
    criado_por_nome  VARCHAR2(120) NOT NULL,
    criado_em        TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    lido_em          TIMESTAMP,
    -- Idempotencia do consumidor: um aviso por evento recebido.
    evento_id        VARCHAR2(64) NOT NULL,
    CONSTRAINT fk_aviso_idoso FOREIGN KEY (idoso_id) REFERENCES TB_USUARIO (id),
    CONSTRAINT uk_aviso_evento UNIQUE (evento_id),
    CONSTRAINT ck_aviso_tipo CHECK (tipo IN ('MEDICAMENTO', 'CONSULTA'))
);

CREATE TABLE TB_RELATORIO_ADESAO (
    id                 NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    idoso_id           NUMBER NOT NULL,
    periodo_inicio     DATE NOT NULL,
    periodo_fim        DATE NOT NULL,
    percentual_adesao  NUMBER(5, 2),
    -- JSON com resumo, pontosDeAtencao e sugestaoParaFamilia. Nulo quando o LLM falhou.
    texto_llm          CLOB,
    -- JSON com os numeros calculados no Oracle (base do texto do LLM).
    dados_json         CLOB NOT NULL,
    gerado_em          TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT fk_relatorio_idoso FOREIGN KEY (idoso_id) REFERENCES TB_USUARIO (id)
);

-- Sem chave estrangeira de proposito: o historico de auditoria sobrevive a exclusao do medicamento.
CREATE TABLE TB_AUDITORIA_MEDICAMENTO (
    id               NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    medicamento_id   NUMBER NOT NULL,
    operacao         VARCHAR2(10) NOT NULL,
    valores_antigos  VARCHAR2(1000),
    valores_novos    VARCHAR2(1000),
    usuario_bd       VARCHAR2(60) DEFAULT USER NOT NULL,
    data_hora        TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
);

CREATE OR REPLACE TRIGGER trg_auditoria_medicamento
AFTER INSERT OR UPDATE OR DELETE ON TB_MEDICAMENTO
FOR EACH ROW
DECLARE
    v_operacao VARCHAR2(10);
BEGIN
    v_operacao := CASE WHEN INSERTING THEN 'INSERT' WHEN UPDATING THEN 'UPDATE' ELSE 'DELETE' END;

    INSERT INTO TB_AUDITORIA_MEDICAMENTO (medicamento_id, operacao, valores_antigos, valores_novos)
    VALUES (
        NVL(:NEW.id, :OLD.id),
        v_operacao,
        CASE WHEN INSERTING THEN NULL
             ELSE 'nome=' || :OLD.nome || ';dosagem=' || :OLD.dosagem || ';critico=' || :OLD.critico
                  || ';ativo=' || :OLD.ativo || ';limite_pulos=' || :OLD.limite_pulos END,
        CASE WHEN DELETING THEN NULL
             ELSE 'nome=' || :NEW.nome || ';dosagem=' || :NEW.dosagem || ';critico=' || :NEW.critico
                  || ';ativo=' || :NEW.ativo || ';limite_pulos=' || :NEW.limite_pulos END
    );
END;
/

-- ===== Indices =====
-- Cada indice atende uma consulta quente da API; sem eles viram varredura completa (O(n)) em vez de O(log n).

-- Lista de medicamentos do idoso (GET /idosos/{id}/medicamentos).
CREATE INDEX idx_medicamento_idoso ON TB_MEDICAMENTO (idoso_id);

-- Horarios de um medicamento (carregados junto com ele).
CREATE INDEX idx_horario_medicamento ON TB_HORARIO_MEDICAMENTO (medicamento_id);

-- Job de 60 s: doses PENDENTE vencidas. Comeca por status e ordena pelo horario.
CREATE INDEX idx_dose_status_horario ON TB_REGISTRO_DOSE (status, horario_previsto);

-- Doses do dia por medicamento e janela de adesao: o UNIQUE (medicamento_id, horario_previsto) ja cobre.

-- Avisos do idoso, mais recentes primeiro, com filtro por lido.
CREATE INDEX idx_aviso_idoso_criado ON TB_AVISO (idoso_id, criado_em DESC);

-- Historico de relatorios do idoso.
CREATE INDEX idx_relatorio_idoso_gerado ON TB_RELATORIO_ADESAO (idoso_id, gerado_em DESC);

-- ===== Pacote de adesao (PL/SQL) =====
-- Regras de calculo que ficam no Oracle (disciplina de banco): adesao e severidade do alerta.
CREATE OR REPLACE PACKAGE pkg_adesao AS
    -- Adesao (%) = TOMADA / (TOMADA + PULADA) no periodo [p_inicio, p_fim). PENDENTES nao entram.
    -- Retorna NULL quando nao ha doses encerradas no periodo.
    FUNCTION fn_percentual_adesao(p_medicamento_id IN NUMBER,
                                  p_inicio         IN TIMESTAMP,
                                  p_fim            IN TIMESTAMP) RETURN NUMBER;

    -- ALTA: medicamento critico. MEDIA: doses PULADA nos 7 dias ate a dose chegam a limite_pulos.
    -- NENHUM: demais casos (nao gera evento).
    FUNCTION fn_severidade_alerta(p_dose_id IN NUMBER) RETURN VARCHAR2;
END pkg_adesao;
/

CREATE OR REPLACE PACKAGE BODY pkg_adesao AS

    FUNCTION fn_percentual_adesao(p_medicamento_id IN NUMBER,
                                  p_inicio         IN TIMESTAMP,
                                  p_fim            IN TIMESTAMP) RETURN NUMBER IS
        v_tomadas NUMBER;
        v_puladas NUMBER;
    BEGIN
        SELECT NVL(SUM(CASE WHEN status = 'TOMADA' THEN 1 ELSE 0 END), 0),
               NVL(SUM(CASE WHEN status = 'PULADA' THEN 1 ELSE 0 END), 0)
          INTO v_tomadas, v_puladas
          FROM TB_REGISTRO_DOSE
         WHERE medicamento_id = p_medicamento_id
           AND horario_previsto >= p_inicio
           AND horario_previsto <  p_fim;

        IF v_tomadas + v_puladas = 0 THEN
            RETURN NULL;
        END IF;
        RETURN ROUND(100 * v_tomadas / (v_tomadas + v_puladas), 2);
    END fn_percentual_adesao;

    FUNCTION fn_severidade_alerta(p_dose_id IN NUMBER) RETURN VARCHAR2 IS
        v_critico  CHAR(1);
        v_limite   NUMBER;
        v_medicamento NUMBER;
        v_horario  TIMESTAMP;
        v_puladas  NUMBER;
    BEGIN
        SELECT m.critico, m.limite_pulos, m.id, d.horario_previsto
          INTO v_critico, v_limite, v_medicamento, v_horario
          FROM TB_REGISTRO_DOSE d
          JOIN TB_MEDICAMENTO m ON m.id = d.medicamento_id
         WHERE d.id = p_dose_id;

        IF v_critico = 'S' THEN
            RETURN 'ALTA';
        END IF;

        SELECT COUNT(*)
          INTO v_puladas
          FROM TB_REGISTRO_DOSE
         WHERE medicamento_id = v_medicamento
           AND status = 'PULADA'
           AND horario_previsto >  v_horario - INTERVAL '7' DAY
           AND horario_previsto <= v_horario;

        IF v_puladas >= v_limite THEN
            RETURN 'MEDIA';
        END IF;
        RETURN 'NENHUM';
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RETURN 'NENHUM';
    END fn_severidade_alerta;

END pkg_adesao;
/
