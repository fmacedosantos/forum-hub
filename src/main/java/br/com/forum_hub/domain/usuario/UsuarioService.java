package br.com.forum_hub.domain.usuario;

import br.com.forum_hub.domain.autenticacao.HierarquiaService;
import br.com.forum_hub.domain.perfil.DadosPerfil;
import br.com.forum_hub.domain.perfil.PerfilNome;
import br.com.forum_hub.domain.perfil.PerfilRepository;
import br.com.forum_hub.infra.email.EmailService;
import br.com.forum_hub.infra.exception.RegraDeNegocioException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    private final PerfilRepository perfilRepository;

    private final HierarquiaService  hierarquiaService;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, EmailService emailService, PerfilRepository perfilRepository, HierarquiaService hierarquiaService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.perfilRepository = perfilRepository;
        this.hierarquiaService = hierarquiaService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByEmailIgnoreCaseAndVerificadoTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("O usuário não foi encontrado!"));
    }

    @Transactional
    public Usuario cadastrar(@Valid DadosCadastroUsuario dados) {
        Optional<Usuario> usuarioExistente = usuarioRepository.findByEmailIgnoreCaseOrNomeUsuarioIgnoreCase(dados.email(), dados.nomeUsuario());

        if (usuarioExistente.isPresent()) {
            throw new RegraDeNegocioException("Já existe uma conta cadastrada com esse email ou nome de usuário!");
        }

        var senhaCriptografada = passwordEncoder.encode(dados.senha());

        var perfil = perfilRepository.findByNome(PerfilNome.ESTUDANTE);

        var usuario = new Usuario(dados, senhaCriptografada, perfil);

        emailService.enviarEmailVerificacao(usuario);

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void verificarEmail(String codigo) {
        var usuario = usuarioRepository.findByToken(codigo).orElseThrow();

        usuario.verificar();
    }

    public Usuario buscarPeloNomeUsuario(String nomeUsuario) {
        return usuarioRepository.findByNomeUsuarioIgnoreCaseAndVerificadoTrueAndAtivoTrue(nomeUsuario).orElseThrow(
                () -> new RegraDeNegocioException("Usuário não encontrado!"));
    }

    @Transactional
    public Usuario editarPerfil(Usuario usuario, DadosEdicaoUsuario dados) {
        return usuario.alterarDados(dados);
    }

    @Transactional
    public void alterarSenha(DadosAlteracaoSenha dados, Usuario logado) {
        if(!passwordEncoder.matches(dados.senhaAtual(), logado.getPassword())){
            throw new RegraDeNegocioException("Senha digitada não confere com senha atual!");
        }

        if(!dados.novaSenha().equals(dados.novaSenhaConfirmacao())){
            throw new RegraDeNegocioException("Senha e confirmação não conferem!");
        }

        String senhaCriptografada = passwordEncoder.encode(dados.novaSenha());
        logado.alterarSenha(senhaCriptografada);
    }

    @Transactional
    public void desativarUsuario(Long id, Usuario logado) {
        var usuario = usuarioRepository.findById(id).orElseThrow();

        if(hierarquiaService.usuarioNaoTemPermissoes(logado, usuario, "ROLE_ADMIN"))
            throw new AccessDeniedException("Não é possivel realizar essa operação!");

        usuario.desativar();
    }

    @Transactional
    public Usuario adicionarPerfil(Long id, @Valid DadosPerfil dados) {
        var usuario = usuarioRepository.findById(id).orElseThrow();
        var perfil = perfilRepository.findByNome(dados.perfilNome());

        usuario.adicionarPerfil(perfil);

        return usuario;
    }

    @Transactional
    public Usuario removerPerfil(Long id, @Valid DadosPerfil dados) {
        var usuario = usuarioRepository.findById(id).orElseThrow();
        var perfil = perfilRepository.findByNome(dados.perfilNome());

        usuario.removerPerfil(perfil);

        return usuario;
    }

    @Transactional
    public void reativarUsuario(Long id) {
        var usuario = usuarioRepository.findById(id).orElseThrow();
        usuario.reativar();
    }
}
