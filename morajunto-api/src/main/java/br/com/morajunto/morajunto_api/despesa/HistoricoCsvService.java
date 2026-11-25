package br.com.morajunto.morajunto_api.despesa;

import br.com.morajunto.morajunto_api.moradia.Moradia;
import br.com.morajunto.morajunto_api.moradia.MoradiaAtivaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoricoCsvService {

    private static final String SQL = """
            select 'Despesa' as tipo, d.data_despesa as data_movimento, d.nome as descricao,
                   c.nome as categoria, d.valor_total as valor, pg.nome as quem_pagou,
                   null as quem_recebeu, d.descricao as observacao
            from despesas d
            join categorias_despesa c on c.id = d.categoria_id
            join moradores pg on pg.id = d.pago_por_morador_id
            where d.moradia_id = :moradiaId and d.data_despesa between :inicio and :fim
            union all
            select 'Pagamento', p.data_pagamento, 'Pagamento entre moradores',
                   null, p.valor, pg.nome,
                   rc.nome, p.observacao
            from pagamentos p
            join moradores pg on pg.id = p.pagador_id
            join moradores rc on rc.id = p.recebedor_id
            where p.moradia_id = :moradiaId and p.data_pagamento between :inicio and :fim
            order by data_movimento, tipo
            """;

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final LocalDate INICIO_PADRAO = LocalDate.of(1000, 1, 1);
    private static final LocalDate FIM_PADRAO = LocalDate.of(9999, 12, 31);

    private final JdbcClient jdbcClient;
    private final MoradiaAtivaService moradiaAtivaService;

    @Transactional(readOnly = true)
    public byte[] gerar(Long usuarioId, LocalDate inicio, LocalDate fim) {
        Moradia moradia = moradiaAtivaService.doMembro(usuarioId);

        LocalDate de = inicio != null ? inicio : INICIO_PADRAO;
        LocalDate ate = fim != null ? fim : FIM_PADRAO;

        if (de.isAfter(ate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data inicial precisa ser antes da data final");
        }

        List<Linha> linhas = jdbcClient.sql(SQL)
                .param("moradiaId", moradia.getId())
                .param("inicio", de)
                .param("fim", ate)
                .query((rs, numero) -> new Linha(
                        rs.getString("tipo"),
                        rs.getObject("data_movimento", LocalDate.class),
                        rs.getString("descricao"),
                        rs.getString("categoria"),
                        rs.getBigDecimal("valor"),
                        rs.getString("quem_pagou"),
                        rs.getString("quem_recebeu"),
                        rs.getString("observacao")
                ))
                .list();

        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("Tipo;Data;Descrição;Categoria;Valor;Quem pagou;Quem recebeu;Observação\r\n");

        for (Linha linha : linhas) {
            csv.append(String.join(";",
                    campo(linha.tipo()),
                    campo(linha.data().format(DATA)),
                    campo(linha.descricao()),
                    campo(linha.categoria()),
                    campo(valor(linha.valor())),
                    campo(linha.quemPagou()),
                    campo(linha.quemRecebeu()),
                    campo(linha.observacao())
            )).append("\r\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String valor(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }

    private String campo(String texto) {
        if (texto == null) {
            return "";
        }
        if (texto.contains(";") || texto.contains("\"") || texto.contains("\n") || texto.contains("\r")) {
            return "\"" + texto.replace("\"", "\"\"") + "\"";
        }
        return texto;
    }

    private record Linha(
            String tipo,
            LocalDate data,
            String descricao,
            String categoria,
            BigDecimal valor,
            String quemPagou,
            String quemRecebeu,
            String observacao
    ) {
    }
}