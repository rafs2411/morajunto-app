package br.com.morajunto.morajunto_api.despesa;

import br.com.morajunto.morajunto_api.moradia.MoradiaAtivaService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SaldoService {

    private static final String SQL_SALDOS = """
            SELECT m.id, m.nome, m.cor, s.saldo
            FROM vw_saldo_morador s
            JOIN moradores m ON m.id = s.morador_id
            WHERE s.moradia_id = :moradiaId
              AND (m.ativo = TRUE OR s.saldo <> 0)
            ORDER BY m.nome
            """;

    private static final String SQL_DIVIDAS = """
            SELECT d.devedor_id, dv.nome AS devedor_nome, dv.cor AS devedor_cor,
                   d.credor_id, cr.nome AS credor_nome, cr.cor AS credor_cor, d.valor
            FROM vw_dividas_entre_moradores d
            JOIN moradores dv ON dv.id = d.devedor_id
            JOIN moradores cr ON cr.id = d.credor_id
            WHERE d.moradia_id = :moradiaId
            ORDER BY d.valor DESC, dv.nome
            """;

    private final JdbcClient jdbcClient;
    private final MoradiaAtivaService moradiaAtivaService;

    @Transactional(readOnly = true)
    public ResumoFinanceiroResponse resumo(Long usuarioId) {
        Long moradiaId = moradiaAtivaService.doMembro(usuarioId).getId();

        List<SaldoResponse> saldos = jdbcClient.sql(SQL_SALDOS)
                .param("moradiaId", moradiaId)
                .query((rs, linha) -> new SaldoResponse(
                        rs.getLong("id"),
                        rs.getString("nome"),
                        rs.getString("cor"),
                        rs.getBigDecimal("saldo")))
                .list();

        List<DividaResponse> dividas = jdbcClient.sql(SQL_DIVIDAS)
                .param("moradiaId", moradiaId)
                .query((rs, linha) -> new DividaResponse(
                        rs.getLong("devedor_id"),
                        rs.getString("devedor_nome"),
                        rs.getString("devedor_cor"),
                        rs.getLong("credor_id"),
                        rs.getString("credor_nome"),
                        rs.getString("credor_cor"),
                        rs.getBigDecimal("valor")))
                .list();

        return new ResumoFinanceiroResponse(saldos, dividas);
    }
}