import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 1. CLASSE DE DOMÍNIO: Tecnico
 * Representa a tabela tb_tecnico (profissionais calibradores e signatários).
 */
class Tecnico {
    private int idTecnico;
    private String nmTecnico;
    private String dsCargo;
    private String dsSetorSolicitante;
    private int flSignatarioAutorizado; // 0 = Não, 1 = Sim

    public Tecnico(String nmTecnico, String dsCargo, String dsSetorSolicitante, int flSignatarioAutorizado) {
        this.nmTecnico = nmTecnico;
        this.dsCargo = dsCargo;
        this.dsSetorSolicitante = dsSetorSolicitante;
        setFlSignatarioAutorizado(flSignatarioAutorizado);
    }

    public Tecnico(int idTecnico, String nmTecnico, String dsCargo, String dsSetorSolicitante, int flSignatarioAutorizado) {
        this(nmTecnico, dsCargo, dsSetorSolicitante, flSignatarioAutorizado);
        this.idTecnico = idTecnico;
    }

    public int getIdTecnico() { return idTecnico; }
    public void setIdTecnico(int idTecnico) { this.idTecnico = idTecnico; }

    public String getNmTecnico() { return nmTecnico; }
    public void setNmTecnico(String nmTecnico) { this.nmTecnico = nmTecnico; }

    public String getDsCargo() { return dsCargo; }
    public void setDsCargo(String dsCargo) { this.dsCargo = dsCargo; }

    public String getDsSetorSolicitante() { return dsSetorSolicitante; }
    public void setDsSetorSolicitante(String dsSetorSolicitante) { this.dsSetorSolicitante = dsSetorSolicitante; }

    public int getFlSignatarioAutorizado() { return flSignatarioAutorizado; }
    public void setFlSignatarioAutorizado(int flSignatarioAutorizado) {
        if (flSignatarioAutorizado != 0 && flSignatarioAutorizado != 1) {
            throw new IllegalArgumentException("Flag de signatário deve ser 0 ou 1.");
        }
        this.flSignatarioAutorizado = flSignatarioAutorizado;
    }
}

/**
 * 2. CLASSE DE DOMÍNIO: Equipamento
 * Representa a tabela tb_equipamento (instrumentos de medição e dispositivos).
 */
class Equipamento {
    private int idEquipamento;
    private String cdEquipamento;
    private String nmDenominacao;
    private String nmFabricante;
    private double vlrNominalGeral;
    private String dsLocalizacao;

    public Equipamento(String cdEquipamento, String nmDenominacao, String nmFabricante, double vlrNominalGeral, String dsLocalizacao) {
        this.cdEquipamento = cdEquipamento;
        this.nmDenominacao = nmDenominacao;
        this.nmFabricante = (nmFabricante == null || nmFabricante.isBlank()) ? "Não consta" : nmFabricante;
        this.vlrNominalGeral = vlrNominalGeral;
        this.dsLocalizacao = dsLocalizacao;
    }

    public Equipamento(int idEquipamento, String cdEquipamento, String nmDenominacao, String nmFabricante, double vlrNominalGeral, String dsLocalizacao) {
        this(cdEquipamento, nmDenominacao, nmFabricante, vlrNominalGeral, dsLocalizacao);
        this.idEquipamento = idEquipamento;
    }

    public int getIdEquipamento() { return idEquipamento; }
    public void setIdEquipamento(int idEquipamento) { this.idEquipamento = idEquipamento; }

    public String getCdEquipamento() { return cdEquipamento; }
    public void setCdEquipamento(String cdEquipamento) { this.cdEquipamento = cdEquipamento; }

    public String getNmDenominacao() { return nmDenominacao; }
    public void setNmDenominacao(String nmDenominacao) { this.nmDenominacao = nmDenominacao; }

    public String getNmFabricante() { return nmFabricante; }
    public void setNmFabricante(String nmFabricante) { this.nmFabricante = nmFabricante; }

    public double getVlrNominalGeral() { return vlrNominalGeral; }
    public void setVlrNominalGeral(double vlrNominalGeral) { this.vlrNominalGeral = vlrNominalGeral; }

    public String getDsLocalizacao() { return dsLocalizacao; }
    public void setDsLocalizacao(String dsLocalizacao) { this.dsLocalizacao = dsLocalizacao; }
}

/**
 * 3. CLASSE DE DOMÍNIO E REGRA DE NEGÓCIO: Medicao
 * Representa a tabela tb_medicao e executa o algoritmo metrológico de aprovação.
 */
class Medicao {
    private int idMedicao;
    private int idEquipamento;
    private int idTecnico;
    private String nrCertificado;
    private String nrOs;
    private String dtCalibracao; // Formato ISO: AAAA-MM-DD
    private String parametroNm;
    private double vlrNominal;
    private double tolSuperior;
    private double tolInferior;
    private double vlrObtido;
    private double vlrErro;
    private double vlrIncertezaExp;
    private String dsStatus;

    public Medicao(int idEquipamento, int idTecnico, String nrCertificado, String nrOs,
                   String dtCalibracao, String parametroNm, double vlrNominal,
                   double tolSuperior, double tolInferior, double vlrObtido, double vlrIncertezaExp) {
        this.idEquipamento = idEquipamento;
        this.idTecnico = idTecnico;
        this.nrCertificado = nrCertificado;
        this.nrOs = nrOs;
        this.dtCalibracao = dtCalibracao;
        this.parametroNm = parametroNm;
        this.vlrNominal = vlrNominal;
        this.tolSuperior = tolSuperior;
        this.tolInferior = tolInferior;
        this.vlrObtido = vlrObtido;
        this.vlrIncertezaExp = vlrIncertezaExp;

        // Calcula o erro e define o status automaticamente ao instanciar
        calcularErroEValidar();
    }

    /**
     * Algoritmo de Validação Metrológica (ISO 9001 / IATF 16949):
     * Calcula o desvio (erro = obtido - nominal) e verifica se o valor obtido
     * encontra-se dentro da faixa de tolerância especificada.
     */
    public final void calcularErroEValidar() {
        this.vlrErro = Math.round((this.vlrObtido - this.vlrNominal) * 10000.0) / 10000.0;

        double limiteInferior = this.vlrNominal - Math.abs(this.tolInferior);
        double limiteSuperior = this.vlrNominal + Math.abs(this.tolSuperior);

        if (this.vlrObtido >= limiteInferior && this.vlrObtido <= limiteSuperior) {
            this.dsStatus = "APROVADO";
        } else {
            this.dsStatus = "REPROVADO";
        }
    }

    public int getIdMedicao() { return idMedicao; }
    public void setIdMedicao(int idMedicao) { this.idMedicao = idMedicao; }

    public int getIdEquipamento() { return idEquipamento; }
    public int getIdTecnico() { return idTecnico; }
    public String getNrCertificado() { return nrCertificado; }
    public String getNrOs() { return nrOs; }
    public String getDtCalibracao() { return dtCalibracao; }
    public String getParametroNm() { return parametroNm; }
    public double getVlrNominal() { return vlrNominal; }
    public double getTolSuperior() { return tolSuperior; }
    public double getTolInferior() { return tolInferior; }
    public double getVlrObtido() { return vlrObtido; }
    public double getVlrErro() { return vlrErro; }
    public double getVlrIncertezaExp() { return vlrIncertezaExp; }
    public String getDsStatus() { return dsStatus; }

    public void setVlrObtido(double vlrObtido) {
        this.vlrObtido = vlrObtido;
        calcularErroEValidar(); // Recalcula automaticamente caso o valor obtido seja atualizado
    }
}

/**
 * 4. CLASSE DE CONEXÃO E PERSISTÊNCIA (DAO): BancoDadosDAO
 * Gerencia a conexão JDBC com SQLite e executa comandos SQL (DDL e CRUD).
 */
class BancoDadosDAO {
    private final String urlConexao;

    public BancoDadosDAO(String caminhoBanco) {
        this.urlConexao = "jdbc:sqlite:" + caminhoBanco;
    }

    /**
     * Abre conexão ativando a integridade referencial (PRAGMA foreign_keys = ON).
     */
        public Connection conectar() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver SQLite JDBC não encontrado no classpath.", e);
        }
        Connection conn = DriverManager.getConnection(urlConexao);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    /**
     * Inicializa o esquema relacional idêntico ao 03_banco.sql caso não exista.
     */
    public void inicializarTabelas() throws SQLException {
        String sqlTecnico = """
            CREATE TABLE IF NOT EXISTS tb_tecnico (
                id_tecnico INTEGER PRIMARY KEY AUTOINCREMENT,
                nm_tecnico TEXT NOT NULL,
                ds_cargo TEXT,
                ds_setor_solicitante TEXT,
                fl_signatario_autorizado INTEGER NOT NULL DEFAULT 0 CHECK (fl_signatario_autorizado IN (0, 1))
            );
        """;

        String sqlEquipamento = """
            CREATE TABLE IF NOT EXISTS tb_equipamento (
                id_equipamento INTEGER PRIMARY KEY AUTOINCREMENT,
                cd_equipamento TEXT NOT NULL UNIQUE,
                nm_denominacao TEXT NOT NULL,
                nm_fabricante TEXT DEFAULT 'Não consta',
                vlr_nominal_geral REAL,
                ds_localizacao TEXT
            );
        """;

        String sqlMedicao = """
            CREATE TABLE IF NOT EXISTS tb_medicao (
                id_medicao INTEGER PRIMARY KEY AUTOINCREMENT,
                id_equipamento INTEGER NOT NULL,
                id_tecnico INTEGER NOT NULL,
                nr_certificado TEXT NOT NULL,
                nr_os TEXT NOT NULL,
                dt_calibracao TEXT NOT NULL,
                parametro_nm TEXT NOT NULL,
                vlr_nominal REAL NOT NULL,
                tol_superior REAL NOT NULL,
                tol_inferior REAL NOT NULL,
                vlr_obtido REAL NOT NULL,
                vlr_erro REAL NOT NULL,
                vlr_incerteza_exp REAL NOT NULL DEFAULT 0.002,
                ds_status TEXT NOT NULL CHECK (ds_status IN ('APROVADO', 'REPROVADO')),
                CONSTRAINT fk_medicao_equipamento FOREIGN KEY (id_equipamento)
                REFERENCES tb_equipamento (id_equipamento) ON DELETE RESTRICT ON UPDATE CASCADE,
             CONSTRAINT fk_medicao_tecnico FOREIGN KEY (id_tecnico)
                 REFERENCES tb_tecnico (id_tecnico) ON DELETE RESTRICT ON UPDATE CASCADE
            );
        """;

        try (Connection conn = conectar(); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlTecnico);
            stmt.execute(sqlEquipamento);
            stmt.execute(sqlMedicao);
        }
    }

    // --- OPERAÇÕES INSERT ---

    public int inserirTecnico(Tecnico tecnico) throws SQLException {
        String sql = "INSERT INTO tb_tecnico (nm_tecnico, ds_cargo, ds_setor_solicitante, fl_signatario_autorizado) VALUES (?, ?, ?, ?)";
        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, tecnico.getNmTecnico());
            pstmt.setString(2, tecnico.getDsCargo());
            pstmt.setString(3, tecnico.getDsSetorSolicitante());
            pstmt.setInt(4, tecnico.getFlSignatarioAutorizado());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGerado = rs.getInt(1);
                    tecnico.setIdTecnico(idGerado);
                    return idGerado;
                }
            }
        }
        return -1;
    }

    public int inserirEquipamento(Equipamento equip) throws SQLException {
        String sql = "INSERT INTO tb_equipamento (cd_equipamento, nm_denominacao, nm_fabricante, vlr_nominal_geral, ds_localizacao) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, equip.getCdEquipamento());
            pstmt.setString(2, equip.getNmDenominacao());
            pstmt.setString(3, equip.getNmFabricante());
            pstmt.setDouble(4, equip.getVlrNominalGeral());
            pstmt.setString(5, equip.getDsLocalizacao());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGerado = rs.getInt(1);
                    equip.setIdEquipamento(idGerado);
                    return idGerado;
                }
            }
        }
        return -1;
    }

    public int inserirMedicao(Medicao med) throws SQLException {
        String sql = """
            INSERT INTO tb_medicao (
                id_equipamento, id_tecnico, nr_certificado, nr_os, dt_calibracao,
                parametro_nm, vlr_nominal, tol_superior, tol_inferior,
                vlr_obtido, vlr_erro, vlr_incerteza_exp, ds_status
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, med.getIdEquipamento());
            pstmt.setInt(2, med.getIdTecnico());
            pstmt.setString(3, med.getNrCertificado());
            pstmt.setString(4, med.getNrOs());
            pstmt.setString(5, med.getDtCalibracao());
            pstmt.setString(6, med.getParametroNm());
            pstmt.setDouble(7, med.getVlrNominal());
            pstmt.setDouble(8, med.getTolSuperior());
            pstmt.setDouble(9, med.getTolInferior());
            pstmt.setDouble(10, med.getVlrObtido());
            pstmt.setDouble(11, med.getVlrErro());
            pstmt.setDouble(12, med.getVlrIncertezaExp());
            pstmt.setString(13, med.getDsStatus());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGerado = rs.getInt(1);
                    med.setIdMedicao(idGerado);
                    return idGerado;
                }
            }
        }
        return -1;
    }

    // --- OPERAÇÃO UPDATE ---

    public void atualizarLeituraMedicao(Medicao med) throws SQLException {
        String sql = "UPDATE tb_medicao SET vlr_obtido = ?, vlr_erro = ?, ds_status = ? WHERE id_medicao = ?";
        try (Connection conn = conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, med.getVlrObtido());
            pstmt.setDouble(2, med.getVlrErro());
            pstmt.setString(3, med.getDsStatus());
            pstmt.setInt(4, med.getIdMedicao());
            pstmt.executeUpdate();
        }
    }

    // --- OPERAÇÃO SELECT COM JOIN ---

    public List<String> gerarRelatorioCalibracaoJoin() throws SQLException {
        List<String> linhasRelatorio = new ArrayList<>();
        String sql = """
            SELECT
                m.nr_certificado,
                m.dt_calibracao,
                e.cd_equipamento,
                e.nm_denominacao,
                t.nm_tecnico,
                m.parametro_nm,
                m.vlr_nominal,
                m.tol_inferior,
                m.tol_superior,
                m.vlr_obtido,
                m.vlr_erro,
                m.vlr_incerteza_exp,
                m.ds_status
            FROM tb_medicao m
            INNER JOIN tb_equipamento e ON m.id_equipamento = e.id_equipamento
            INNER JOIN tb_tecnico t ON m.id_tecnico = t.id_tecnico
            ORDER BY m.id_medicao ASC
        """;

        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String registro = String.format(
                    "Certificado: %-10s | Data: %s | Tag: %-10s (%s)%n" +
                    "  Técnico: %-22s | Parâmetro: %s%n" +
                    "  Nominal: %8.3f mm (-%.3f / +%.3f) | Obtido: %8.3f mm | Erro: %+7.4f mm | U: ±%.3f | STATUS: [%s]",
                    rs.getString("nr_certificado"),
                    rs.getString("dt_calibracao"),
                    rs.getString("cd_equipamento"),
                    rs.getString("nm_denominacao"),
                    rs.getString("nm_tecnico"),
                    rs.getString("parametro_nm"),
                    rs.getDouble("vlr_nominal"),
                    Math.abs(rs.getDouble("tol_inferior")),
                    Math.abs(rs.getDouble("tol_superior")),
                    rs.getDouble("vlr_obtido"),
                    rs.getDouble("vlr_erro"),
                    rs.getDouble("vlr_incerteza_exp"),
                    rs.getString("ds_status")
                );
                linhasRelatorio.add(registro);
            }
        }
        return linhasRelatorio;
    }
}

