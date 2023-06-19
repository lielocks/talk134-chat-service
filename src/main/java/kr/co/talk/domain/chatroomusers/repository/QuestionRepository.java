package kr.co.talk.domain.chatroomusers.repository;

import kr.co.talk.domain.chatroomusers.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByKeyword_KeywordId(Long keywordId);
    List<Question> findByKeyword_KeywordIdAndAndStatusMap(Long keywordId, int statusNumber);
}
