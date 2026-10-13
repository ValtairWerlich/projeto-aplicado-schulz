-- ====================================================================
-- SCRIPT DDL DE CRIAÇÃO DAS TABELAS - SCHULZ S.A. METROLOGIA
-- BANCO DE DADOS: SQLite
-- ====================================================================

PRAGMA foreign_keys = ON;

-- 1. TABELA: tb_item_calibrado
CREATE TABLE IF NOT EXISTS tb_item_calibrado (
    id_item INTEGER PRIMARY KEY AUTOINCREMENT,
    cd_instrumento TEXT NOT NULL,
    nm_denominacao TEXT NOT NULL,
    nm_fabricante TEXT,
    vlr_nominal_geral REAL
);

-- 2. TABELA: tb_certificado_calibracao
CREATE TABLE IF NOT EXISTS tb_certificado_calibracao (
    id_certificado INTEGER PRIMARY KEY AUTOINCREMENT,
    nr_certificado TEXT NOT NULL UNIQUE,
    nr_os TEXT NOT NULL,
    dt_calibracao TEXT NOT NULL,
    dt_validade TEXT,
    nm_tecnico TEXT NOT NULL,
    nm_solicitante TEXT NOT NULL,
    vlr_temperatura_ambiente REAL,
    ds_observacoes TEXT
);

-- 3. TABELA: tb_resultado_medicao
CREATE TABLE IF NOT EXISTS tb_resultado_medicao (
    id_resultado INTEGER PRIMARY KEY AUTOINCREMENT,
    id_certificado INTEGER NOT NULL,
    id_item INTEGER NOT NULL,
    nm_parametro TEXT NOT NULL,
    vlr_nominal REAL NOT NULL,
    tol_superior REAL NOT NULL,
    tol_inferior REAL NOT NULL,
    vlr_medida_1 REAL NOT NULL,
    vlr_medida_2 REAL,
    vlr_media REAL NOT NULL,
    vlr_desvio_padrao REAL NOT NULL,
    vlr_erro REAL NOT NULL,
    vlr_incerteza_exp REAL NOT NULL,
    vlr_fator_k REAL NOT NULL,
    str_status_avaliacao TEXT NOT NULL,
    
    FOREIGN KEY (id_certificado) REFERENCES tb_certificado_calibracao (id_certificado) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (id_item) REFERENCES tb_item_calibrado (id_item) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_certificado_os ON tb_certificado_calibracao(nr_os);
CREATE INDEX IF NOT EXISTS idx_item_tag ON tb_item_calibrado(cd_instrumento);
CREATE INDEX IF NOT EXISTS idx_resultado_certificado ON tb_resultado_medicao(id_certificado);
