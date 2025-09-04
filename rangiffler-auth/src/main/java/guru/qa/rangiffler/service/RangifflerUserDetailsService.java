package guru.qa.rangiffler.service;

import guru.qa.rangiffler.data.repository.UserRepository;
import guru.qa.rangiffler.domain.RangifflerUserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RangifflerUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Autowired
  public RangifflerUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository.findByUsername(username)
        .map(RangifflerUserPrincipal::new)
        .orElseThrow(() -> new UsernameNotFoundException("Username: `" + username + "` not found"));
  }
}
