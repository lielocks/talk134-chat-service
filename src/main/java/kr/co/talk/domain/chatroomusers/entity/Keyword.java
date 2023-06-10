package kr.co.talk.domain.chatroomusers.entity;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@ToString
public class Keyword {
    @Id
    private Long keywordId;

    private String name;
    private String depth;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "keyword", fetch = FetchType.LAZY)
    private List<Question> questions;
}
