package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.MemberRepositoryNew;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final MemberRepositoryNew memberRepositoryNew; // Spring Data JPA 활용

    /**
     * 회원가입
     */
    @Transactional
    public Long join(Member member) {
        validateDuplicateMember(member); // 중복회원 검증

        memberRepository.save(member);

        //memberRepositoryNew.save(member);

        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        // Exception
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if(!findMembers.isEmpty()) {
            throw new IllegalStateException("Duplicate member: " + member.getName());
        }
    }

    /**
     * 회원 전체조회
     */
    public List<Member> findMembers() {
        //memberRepositoryNew.findAll();

        return memberRepository.findAll();
    }

    /**
     * 회원 단건조회
     */
    public Member findOne(Long id) {
        return memberRepository.findOne(id);
    }

    @Transactional
    public void update(Long id, String name) {
        Member member = memberRepository.findOne(id);
        //Member memberNew = memberRepositoryNew.findById(id).get();
        member.setName(name);
    }
}
