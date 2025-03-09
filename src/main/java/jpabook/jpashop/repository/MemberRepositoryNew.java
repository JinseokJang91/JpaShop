package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepositoryNew extends JpaRepository<Member, Long> {

    // 따로 구현체에 로직을 작성하지 않아도 JPA에서 select m from Member m where m.name = ? 이라는 JPQL을 만들어 실행
    List<Member> findByName(String name); // findBy"Name"

    // (참고) generated 패키지는 실제 프로젝트 개발 시에는 .gitignore에 추가하여 제외하여야 함.
}
