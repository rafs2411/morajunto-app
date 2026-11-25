package br.com.morajunto.morajunto_api.compra;

import br.com.morajunto.morajunto_api.moradia.Moradia;
import br.com.morajunto.morajunto_api.moradia.MoradiaAtivaService;
import br.com.morajunto.morajunto_api.morador.Morador;
import br.com.morajunto.morajunto_api.morador.MoradorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CompraService {

    private final ItemCompraRepository itemRepository;
    private final MoradorRepository moradorRepository;
    private final MoradiaAtivaService moradiaAtivaService;

    @Transactional(readOnly = true)
    public ListaCompraResponse listar(Long usuarioId) {
        Moradia moradia = moradiaAtivaService.doMembro(usuarioId);
        return new ListaCompraResponse(
                itemRepository.findDaMoradia(moradia.getId(), false).stream().map(this::paraResponse).toList(),
                itemRepository.findDaMoradia(moradia.getId(), true).stream().map(this::paraResponse).toList()
        );
    }

    @Transactional
    public ItemCompraResponse criar(Long usuarioId, ItemCompraRequest request) {
        Moradia moradia = moradiaAtivaService.doMembro(usuarioId);
        Morador quem = moradorDoUsuario(usuarioId, moradia.getId());

        ItemCompra item = new ItemCompra();
        item.setMoradia(moradia);
        item.setCriadoPor(quem);
        aplicar(item, request);

        return paraResponse(itemRepository.save(item));
    }

    @Transactional
    public ItemCompraResponse editar(Long usuarioId, Long id, ItemCompraRequest request) {
        Moradia moradia = moradiaAtivaService.doMembro(usuarioId);
        ItemCompra item = buscar(id, moradia.getId());
        aplicar(item, request);
        return paraResponse(itemRepository.save(item));
    }

    @Transactional
    public void excluir(Long usuarioId, Long id) {
        Moradia moradia = moradiaAtivaService.doMembro(usuarioId);
        ItemCompra item = buscar(id, moradia.getId());
        itemRepository.delete(item);
    }

    @Transactional
    public ItemCompraResponse marcarComprado(Long usuarioId, Long id) {
        Moradia moradia = moradiaAtivaService.doMembro(usuarioId);
        ItemCompra item = buscar(id, moradia.getId());
        Morador quem = moradorDoUsuario(usuarioId, moradia.getId());

        item.setComprado(true);
        item.setCompradoEm(LocalDateTime.now());
        item.setCompradoPor(quem);
        return paraResponse(itemRepository.save(item));
    }

    @Transactional
    public ItemCompraResponse desmarcarComprado(Long usuarioId, Long id) {
        Moradia moradia = moradiaAtivaService.doMembro(usuarioId);
        ItemCompra item = buscar(id, moradia.getId());

        item.setComprado(false);
        item.setCompradoEm(null);
        item.setCompradoPor(null);
        return paraResponse(itemRepository.save(item));
    }

    private ItemCompra buscar(Long id, Long moradiaId) {
        return itemRepository.findPorId(id, moradiaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item não encontrado"));
    }

    private Morador moradorDoUsuario(Long usuarioId, Long moradiaId) {
        return moradorRepository.findAtivosDoUsuario(usuarioId).stream()
                .filter(m -> m.getMoradia().getId().equals(moradiaId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Você não faz parte desta moradia"));
    }

    private void aplicar(ItemCompra item, ItemCompraRequest request) {
        item.setNome(request.nome().trim());
        item.setQuantidade(request.quantidade() == null || request.quantidade().isBlank()
                ? null
                : request.quantidade().trim());
    }

    private ItemCompraResponse paraResponse(ItemCompra item) {
        Morador comprador = item.getCompradoPor();
        Morador criador = item.getCriadoPor();
        return new ItemCompraResponse(
                item.getId(),
                item.getNome(),
                item.getQuantidade(),
                item.isComprado(),
                item.getCompradoEm(),
                comprador == null ? null : comprador.getId(),
                comprador == null ? null : comprador.getNome(),
                criador == null ? null : criador.getId(),
                criador == null ? null : criador.getNome(),
                item.getCriadoEm()
        );
    }
}