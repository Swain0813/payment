package com.payment.permission.utils;
import com.payment.common.vo.SysUserVO;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;

@Getter
@Setter
public class SpringSecurityUser implements UserDetails {
    @Delegate
    SysUserVO sysUser;

    private Collection<? extends GrantedAuthority> authorities;
    private boolean credentialsNonExpired = true;
}
