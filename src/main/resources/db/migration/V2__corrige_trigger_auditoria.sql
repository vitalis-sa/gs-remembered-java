-- A versao da V1 compila INVALID (ORA-00920): INSERTING e DELETING sao predicados de PL/SQL e
-- nao valem dentro do INSERT, que e SQL. Com o trigger invalido, todo INSERT/UPDATE/DELETE em
-- TB_MEDICAMENTO falha com ORA-04098. Aqui os valores sao montados em variaveis antes do INSERT.
-- A V1 fica intacta porque o Flyway ja gravou o checksum dela nos bancos onde rodou.
CREATE OR REPLACE TRIGGER trg_auditoria_medicamento
AFTER INSERT OR UPDATE OR DELETE ON TB_MEDICAMENTO
FOR EACH ROW
DECLARE
    v_operacao        VARCHAR2(10);
    v_valores_antigos VARCHAR2(1000);
    v_valores_novos   VARCHAR2(1000);
BEGIN
    IF INSERTING THEN
        v_operacao := 'INSERT';
    ELSIF UPDATING THEN
        v_operacao := 'UPDATE';
    ELSE
        v_operacao := 'DELETE';
    END IF;

    IF NOT INSERTING THEN
        v_valores_antigos := SUBSTR('nome=' || :OLD.nome || ';dosagem=' || :OLD.dosagem || ';critico=' || :OLD.critico
            || ';ativo=' || :OLD.ativo || ';limite_pulos=' || :OLD.limite_pulos, 1, 1000);
    END IF;

    IF NOT DELETING THEN
        v_valores_novos := SUBSTR('nome=' || :NEW.nome || ';dosagem=' || :NEW.dosagem || ';critico=' || :NEW.critico
            || ';ativo=' || :NEW.ativo || ';limite_pulos=' || :NEW.limite_pulos, 1, 1000);
    END IF;

    INSERT INTO TB_AUDITORIA_MEDICAMENTO (medicamento_id, operacao, valores_antigos, valores_novos)
    VALUES (NVL(:NEW.id, :OLD.id), v_operacao, v_valores_antigos, v_valores_novos);
END;
/
