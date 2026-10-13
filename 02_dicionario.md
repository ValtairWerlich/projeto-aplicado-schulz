# Dicionário de Dados do Sistema Unificado (Schulz S.A.)

A tabela abaixo mapeia os principais campos extraídos das fontes de dados operacionais (LightLab/Calypso), definindo seus respectivos tipos de dados nativos para compatibilidade com o SQLite.

| Campo | Entidade / Tabela | Tipo de Dado (SQLite) | Descrição / Exemplo | Restrições |
| :--- | :--- | :--- | :--- | :--- |
| `id_certificado` | tb_certificado_calibracao | Inteiro (INTEGER) | Chave primária interna do certificado. | PK, AUTOINCREMENT |
| `nr_certificado` | tb_certificado_calibracao | Texto (TEXT) | Código oficial do laudo emitido (Ex: '00091/2026'). | NOT NULL, UNIQUE |
| `nr_os` | tb_certificado_calibracao | Texto (TEXT) | Número da Ordem de Serviço alfanumérica para rastreabilidade. | NOT NULL |
| `dt_calibracao` | tb_certificado_calibracao | Texto (TEXT) | Data do ensaio metrológico gravada no formato YYYY-MM-DD. | NOT NULL |
| `nm_tecnico` | tb_certificado_calibracao | Texto (TEXT) | Nome completo do metrologista executor após higienização. | NOT NULL |
| `id_item` | tb_item_calibrado | Inteiro (INTEGER) | Identificador único interno do instrumento. | PK, AUTOINCREMENT |
| `cd_instrumento` | tb_item_calibrado | Texto (TEXT) | Código interno ou TAG de patrimônio do ativo (Ex: '703.1208'). | NOT NULL |
| `nm_denominacao` | tb_item_calibrado | Texto (TEXT) | Identificação descritiva (Ex: 'Dispositivo de controle'). | NOT NULL |
| `id_resultado` | tb_resultado_medicao | Inteiro (INTEGER) | Chave primária que identifica a linha do resultado. | PK, AUTOINCREMENT |
| `nm_parametro` | tb_resultado_medicao | Texto (TEXT) | Nome descritivo da cota geométrica (Ex: '62P_DIÂMETRO 1'). | NOT NULL |
| `vlr_nominal` | tb_resultado_medicao | Real (REAL) | Ponto central projetado da cota sob análise técnica. | NOT NULL |
| `tol_superior` | tb_resultado_medicao | Real (REAL) | Limite máximo superior admissível da tolerância de projeto. | NOT NULL |
| `tol_inferior` | tb_resultado_medicao | Real (REAL) | Limite mínimo inferior admissível da tolerância de projeto. | NOT NULL |
| `vlr_medida_1` | tb_resultado_medicao | Real (REAL) | Registro numérico da primeira leitura obtida na máquina MMC. | NOT NULL |
| `vlr_media` | tb_resultado_medicao | Real (REAL) | Média aritmética calculada das leituras realizadas no ensaio. | NOT NULL |
| `vlr_erro` | tb_resultado_medicao | Real (REAL) | Diferença matemática entre a média obtida e o valor nominal. | NOT NULL |
| `vlr_incerteza_exp`| tb_resultado_medicao | Real (REAL) | Resultado final do cálculo de incerteza expandida em mm. | NOT NULL |
| `vlr_fator_k` | tb_resultado_medicao | Real (REAL) | Fator de abrangência estatística associado (Ex: 2.23). | NOT NULL |
