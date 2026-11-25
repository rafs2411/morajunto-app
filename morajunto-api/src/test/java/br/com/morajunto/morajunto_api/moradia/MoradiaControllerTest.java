package br.com.morajunto.morajunto_api.moradia;

import br.com.morajunto.morajunto_api.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MoradiaController.class)
@Import(SecurityConfig.class)
class MoradiaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MoradiaService moradiaService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("Sem login a API responde 401")
    void semLogin() throws Exception {
        mockMvc.perform(get("/api/moradias"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Com login a API lista as moradias do usuário do token")
    void comLoginListaMoradias() throws Exception {
        when(moradiaService.listar(1L)).thenReturn(List.of(
                new MoradiaResponse(1L, "Casa da Galera", "Rua das Palmeiras, 127", 1L, 3, true, true, (short) 2)));

        mockMvc.perform(get("/api/moradias").with(maria()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Casa da Galera"))
                .andExpect(jsonPath("$[0].totalMoradores").value(3))
                .andExpect(jsonPath("$[0].vagas").value(2));
    }

    @Test
    @DisplayName("Dados inválidos voltam com a lista de campos em português")
    void dadosInvalidos() throws Exception {
        mockMvc.perform(post("/api/moradias").with(maria())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"endereco\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Dados inválidos"))
                .andExpect(jsonPath("$.erros.nome").value("Informe o nome da moradia"))
                .andExpect(jsonPath("$.erros.endereco").value("Informe o endereço"));

        verify(moradiaService, never()).criar(any(), any());
    }

    @Test
    @DisplayName("Erro lançado pelo service vira JSON com status e mensagem")
    void erroDoServiceFormatado() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Só o dono pode alterar esta moradia"))
                .when(moradiaService).excluir(1L, 1L);

        mockMvc.perform(delete("/api/moradias/1").with(maria()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.mensagem").value("Só o dono pode alterar esta moradia"));
    }

    @Test
    @DisplayName("Criar moradia válida devolve 201")
    void criarMoradiaValida() throws Exception {
        when(moradiaService.criar(eq(1L), any(MoradiaRequest.class))).thenReturn(
                new MoradiaResponse(4L, "Apê Novo", "Rua Nova, 10", 1L, 1, true, false, null));

        mockMvc.perform(post("/api/moradias").with(maria())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Apê Novo\",\"endereco\":\"Rua Nova, 10\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.disponivelParaEntrada").value(false));
    }

    private RequestPostProcessor maria() {
        return jwt().jwt(token -> token.subject("1"));
    }
}