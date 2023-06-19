package kr.co.talk.domain.chatroomusers.entity;

import lombok.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@ToString
public class Question {
    @Id
    private Long questionId;

    private String content;

    private int statusMap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_id")
    private Keyword keyword;
    
    private String guide;
    
    public List<String> getGuideList() {
        return Stream.of(guide.split("\n"))
                .map(s -> s.replaceAll("- ", ""))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
