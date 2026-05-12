package com.example.pagebuilder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    // ✅ Team_Id navega por la relación @ManyToOne correctamente
    List<Member> findByTeam_Id(Long teamId);
}