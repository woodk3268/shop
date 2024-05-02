package com.shop.service;

import com.shop.entity.Member;
import com.shop.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {
    private final MemberRepository memberRepository;

    public Member saveMember(Member member){
        //중복회원 검사
        validateDuplicateMember(member);
        //저장
        return memberRepository.save(member);
    }
    private void validateDuplicateMember(Member member){
        // 이메일로 repository에서 찾아서 존재하면 exception 던짐
        Member findMember = memberRepository.findByEmail(member.getEmail());
        if(findMember!= null){
            throw new IllegalStateException("이미 가입된 회원입니다.");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        //이메일로 repository에서 찾기
        Member member = memberRepository.findByEmail(email);
        //없으면 exception
        if(member==null){
            throw new UsernameNotFoundException(email);

        }
        //있으면 username, password ,roles 담아서 User 객체 생성
        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .roles(member.getRole().toString())
                .build();
    }
}
