-- Dados de teste fictícios (LGPD: nenhum dado real) para a correção e para a demonstração.
-- Os idosos entram com a senha de teste Remembered@2026, guardada aqui apenas como hash BCrypt.
--
-- Cada INSERT só grava se o registro ainda não existe. Assim, a migration também roda em
-- schemas que já têm esses e-mails cadastrados, sem violar a chave única de e-mail.
-- No Oracle da Azure (container sem disco persistente), o Flyway reaplica esta carga sempre
-- que o schema é recriado.

INSERT INTO TB_USUARIO (nome, email, senha_hash)
SELECT 'Maria Aparecida', 'maria.idosa@teste.dev', '$2a$10$OI9lPNKogSD5K4wRVA8eJOp49G.EUpxqm2k7IkNJ2fBV0SqvprIpu'
  FROM dual
 WHERE NOT EXISTS (SELECT 1 FROM TB_USUARIO WHERE email = 'maria.idosa@teste.dev');

INSERT INTO TB_USUARIO (nome, email, senha_hash)
SELECT 'José Carlos', 'jose.idoso@teste.dev', '$2a$10$OI9lPNKogSD5K4wRVA8eJOp49G.EUpxqm2k7IkNJ2fBV0SqvprIpu'
  FROM dual
 WHERE NOT EXISTS (SELECT 1 FROM TB_USUARIO WHERE email = 'jose.idoso@teste.dev');

INSERT INTO TB_MEDICAMENTO (idoso_id, nome, dosagem, forma, critico, data_inicio, criado_por_tipo, criado_por_nome)
SELECT u.id, 'Losartana', '50 mg', 'Comprimido', 'S', TRUNC(SYSDATE), 'IDOSO', u.nome
  FROM TB_USUARIO u
 WHERE u.email = 'maria.idosa@teste.dev'
   AND NOT EXISTS (SELECT 1 FROM TB_MEDICAMENTO m WHERE m.idoso_id = u.id AND m.nome = 'Losartana');

INSERT INTO TB_MEDICAMENTO (idoso_id, nome, dosagem, forma, critico, data_inicio, criado_por_tipo, criado_por_nome)
SELECT u.id, 'Metformina', '850 mg', 'Comprimido', 'N', TRUNC(SYSDATE), 'IDOSO', u.nome
  FROM TB_USUARIO u
 WHERE u.email = 'maria.idosa@teste.dev'
   AND NOT EXISTS (SELECT 1 FROM TB_MEDICAMENTO m WHERE m.idoso_id = u.id AND m.nome = 'Metformina');

INSERT INTO TB_MEDICAMENTO (idoso_id, nome, dosagem, forma, critico, data_inicio, criado_por_tipo, criado_por_nome)
SELECT u.id, 'Sinvastatina', '20 mg', 'Comprimido', 'N', TRUNC(SYSDATE), 'IDOSO', u.nome
  FROM TB_USUARIO u
 WHERE u.email = 'jose.idoso@teste.dev'
   AND NOT EXISTS (SELECT 1 FROM TB_MEDICAMENTO m WHERE m.idoso_id = u.id AND m.nome = 'Sinvastatina');

-- Horários: um INSERT por medicamento e hora, também só se ainda não existir.
INSERT INTO TB_HORARIO_MEDICAMENTO (medicamento_id, hora)
SELECT m.id, h.hora
  FROM TB_MEDICAMENTO m
  JOIN TB_USUARIO u ON u.id = m.idoso_id
  JOIN (SELECT 'maria.idosa@teste.dev' email, 'Losartana' nome, '08:00' hora FROM dual
        UNION ALL SELECT 'maria.idosa@teste.dev', 'Losartana', '20:00' FROM dual
        UNION ALL SELECT 'maria.idosa@teste.dev', 'Metformina', '12:00' FROM dual
        UNION ALL SELECT 'jose.idoso@teste.dev', 'Sinvastatina', '21:00' FROM dual) h
    ON h.email = u.email AND h.nome = m.nome
 WHERE NOT EXISTS (SELECT 1 FROM TB_HORARIO_MEDICAMENTO x WHERE x.medicamento_id = m.id AND x.hora = h.hora);
