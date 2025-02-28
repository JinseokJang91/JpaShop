package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

//    @PersistenceContext
//    private EntityManager em; // 스프링부트에선 의존성 주입으로 @PersistenceContext 어노테이션 생략 가능

    private final EntityManager em;

    public void save(final Member member) {
        em.persist(member);
    }

    public Member findOne(final Long id) {
        return em.find(Member.class, id);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class) // JPQL 작성, 엔티티에 대한 쿼리(테이블 X)
                .getResultList();
    }

    public List<Member> findByName(final String name) {
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }
}
