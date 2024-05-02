package com.shop.controller;

import com.shop.dto.MemberFormDto;
import com.shop.entity.Member;
import com.shop.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/members")
@Controller
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/new")
    public String memberForm(Model model){
        //빈 dto 모델에 담아서 memberForm 렌더링
        model.addAttribute("memberFormDto", new MemberFormDto());
        return "member/memberForm";
    }
    @PostMapping("/new")
    public String memberForm(@Valid MemberFormDto memberFormDto,
                             BindingResult bindingResult, Model model){
        //dto에서 유효성 검사. 에러 있으면 form 렌더링
        if(bindingResult.hasErrors()){
            return "member/memberForm";
        }
        //에러 없으면 Member 엔티티 만든 다음
        //memberservice 호출. 중복회원 검사한 뒤 repository에 저장
        try{
            Member member = Member.createMember(memberFormDto, passwordEncoder);
            memberService.saveMember(member);
        }catch(IllegalStateException e){
            //중복 회원 있으면 errorMessage 담아서 form 렌더링
            model.addAttribute("errorMessage",e.getMessage());
            return "member/memberForm";
        }
        //저장 성공하면 홈으로 리다이렉트
        return "redirect:/";
    }
    @GetMapping("/login")
    public String loginMember(){
        //memberLoginForm 렌더링
        return "member/memberLoginForm";
    }
    @GetMapping("/login/error")
    public String loginError(Model model){
        //에러 메시지 담아서 memberLoginForm 렌더링
        model.addAttribute("loginErrorMsg", "아이디 또는 비밀번호를 확인해주세요");
        return "member/memberLoginForm";
    }


}
