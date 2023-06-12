package kr.co.talk.domain.chatroomusers.repository;

import kr.co.talk.domain.chatroomusers.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    Keyword findByKeywordId(Long keywordId);
}
