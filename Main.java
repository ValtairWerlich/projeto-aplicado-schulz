import java.sql.SQLException;
import java.util.List;

/**
 * CLASSE PRINCIPAL DE DEMONSTRAÇÃO (MVP - PARTE 1)
 * Projeto Aplicado I — Automação de Certificados de Calibração (Schulz S.A.)
 */
public class Main {
    public static void main(String[] args) {
        // Utiliza um banco dedicado ao teste do MVP para execução limpa e reproduzível
        BancoDadosDAO dao = new BancoDadosDAO("calibracao_schulz_mvp.db");

        System.out.println("=========================================================================");
        System.out.println("   SCHULZ S.A. — SISTEMA DE AUTOMAÇÃO DE CERTIFICADOS DE CALIBRAÇÃO");
        System.out.println("=========================================================================\n");

        try {
            // 1. Criação da estrutura relacional SQLite
            dao.inicializarTabelas();
            System.out.println("[OK] Banco de dados SQLite inicializado com integridade referencial (FK).");

            // 2. Instanciação e persistência dos Técnicos Metrologistas
            Tecnico tec1 = new Tecnico("Carlos Eduardo Silva", "Metrologista Sênior", "Laboratório Dimensional", 1);
            Tecnico tec2 = new Tecnico("Mariana Souza Costa", "Técnica em Metrologia", "Usinagem Pesada", 0);

            int idTec1 = dao.inserirTecnico(tec1);
            int idTec2 = dao.inserirTecnico(tec2);
            System.out.println("[OK] Técnicos cadastrados no banco (IDs: " + idTec1 + ", " + idTec2 + ").");

            // 3. Instanciação e persistência dos Equipamentos / Dispositivos
            Equipamento eq1 = new Equipamento("DISP-0452", "Dispositivo de Controle Bloco Compressor", "Schulz / Zeiss", 50.000, "Linha de Usinagem 02");
            Equipamento eq2 = new Equipamento("MIC-0189", "Micrômetro Externo Digital 0-25mm", "Mitutoyo", 25.000, "Sala de Metrologia");

            int idEq1 = dao.inserirEquipamento(eq1);
            int idEq2 = dao.inserirEquipamento(eq2);
            System.out.println("[OK] Equipamentos cadastrados no banco (IDs: " + idEq1 + ", " + idEq2 + ").");

            // 4. Instanciação e persistência das Medições (Ensaios Calypso/Zeiss)
            // Caso 1: Dentro da tolerância (50.000 ± 0.050 -> obtido 50.018) => APROVADO
            Medicao med1 = new Medicao(
                idEq1, idTec1, "CERT-2026-001", "OS-8841", "2026-11-18",
                "Diâmetro Furo Mancal Principal", 50.000, 0.050, 0.050, 50.018, 0.002
            );

            // Caso 2: Fora da tolerância superior (25.000 ± 0.010 -> obtido 25.024) => REPROVADO
            Medicao med2 = new Medicao(
                idEq2, idTec2, "CERT-2026-002", "OS-8842", "2026-11-19",
                "Planicidade Batente Fixo", 25.000, 0.010, 0.010, 25.024, 0.002
            );

            // Caso 3: Inicialmente reprovado, depois corrigido via UPDATE para demonstrar CRUD completo
            Medicao med3 = new Medicao(
                idEq1, idTec1, "CERT-2026-003", "OS-8843", "2026-11-20",
                "Distância Entre Centros", 120.000, 0.030, 0.030, 120.045, 0.003
            );

            dao.inserirMedicao(med1);
            dao.inserirMedicao(med2);
            dao.inserirMedicao(med3);
            System.out.println("[OK] Medições inseridas e validadas automaticamente.");

            // 5. Demonstração de UPDATE (recalibração/ajuste de leitura na med3)
            med3.setVlrObtido(119.992); // Recalcula erro (-0.008) e muda status para APROVADO
            dao.atualizarLeituraMedicao(med3);
            System.out.println("[OK] Atualização (UPDATE) aplicada ao certificado " + med3.getNrCertificado() + ".\n");

            // 6. Consulta Relacional com JOIN e Exibição do Relatório no Terminal
            System.out.println("-------------------------------------------------------------------------");
            System.out.println("   RELATÓRIO CONSOLIDADO DE CALIBRAÇÃO (CONSULTA RELACIONAL JOIN)");
            System.out.println("-------------------------------------------------------------------------");

            List<String> relatorio = dao.gerarRelatorioCalibracaoJoin();
            for (String item : relatorio) {
                System.out.println(item);
                System.out.println("-------------------------------------------------------------------------");
            }

        } catch (SQLException e) {
            System.err.println("[ERRO SQL] Falha na operação com o banco de dados SQLite:");
            System.err.println("Código: " + e.getErrorCode() + " | Mensagem: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("[ERRO DE VALIDAÇÃO] Dado inconsistente informado: " + e.getMessage());
        }
    }
}
