package fixture;

import kr.co.talk.domain.chatroomusers.entity.Keyword;
import kr.co.talk.domain.chatroomusers.entity.Question;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class KeywordFixture {

    public List<Keyword> setUpKeywordMockData() {
        Keyword life =
                Keyword.builder().keywordId(1L).name("일상").build();
        Keyword relationship =
                Keyword.builder().keywordId(2L).name("관계").build();
        Keyword myself =
                Keyword.builder().keywordId(3L).name("나").build();

        return Arrays.asList(life, relationship, myself);
    }

    public List<Question> setUpQuestionMockData() {
        Keyword life =
                Keyword.builder().keywordId(1L).name("일상").build();
        Keyword relationship =
                Keyword.builder().keywordId(2L).name("관계").build();
        Keyword myself =
                Keyword.builder().keywordId(3L).name("나").build();

        return Arrays.asList(
                        Question.builder().questionId(1L).keyword(life).content("1-1").statusMap(1).guide("11").build(),
                        Question.builder().questionId(2L).keyword(life).content("1-2").statusMap(2).guide("12").build(),
                        Question.builder().questionId(3L).keyword(life).content("1-3").statusMap(5).guide("13").build(),
                        Question.builder().questionId(4L).keyword(life).content("1-4").statusMap(4).guide("14").build(),
                        Question.builder().questionId(5L).keyword(life).content("1-5").statusMap(3).guide("15").build(),
                        Question.builder().questionId(6L).keyword(life).content("1-6").statusMap(1).guide("16").build(),
                        Question.builder().questionId(7L).keyword(life).content("1-7").statusMap(5).guide("17").build(),
                        Question.builder().questionId(8L).keyword(life).content("1-8").statusMap(4).guide("18").build(),
                        Question.builder().questionId(9L).keyword(life).content("1-8").statusMap(3).guide("19").build(),
                        Question.builder().questionId(10L).keyword(life).content("1-8").statusMap(2).guide("10").build(),

                        Question.builder().questionId(11L).keyword(relationship).content("2-1").statusMap(0).guide("21").build(),
                        Question.builder().questionId(12L).keyword(relationship).content("2-2").statusMap(0).guide("22").build(),
                        Question.builder().questionId(13L).keyword(relationship).content("2-3").statusMap(2).guide("23").build(),
                        Question.builder().questionId(14L).keyword(relationship).content("2-4").statusMap(5).guide("24").build(),
                        Question.builder().questionId(15L).keyword(relationship).content("2-5").statusMap(3).guide("25").build(),
                        Question.builder().questionId(16L).keyword(relationship).content("2-6").statusMap(2).guide("26").build(),
                        Question.builder().questionId(17L).keyword(relationship).content("2-7").statusMap(5).guide("27").build(),
                        Question.builder().questionId(18L).keyword(relationship).content("2-8").statusMap(1).guide("28").build(),
                        Question.builder().questionId(19L).keyword(relationship).content("2-9").statusMap(0).guide("29").build(),
                        Question.builder().questionId(20L).keyword(relationship).content("2-10").statusMap(0).guide("20").build(),

                        Question.builder().questionId(21L).keyword(myself).content("3-1").statusMap(3).guide("31").build(),
                        Question.builder().questionId(22L).keyword(myself).content("3-2").statusMap(4).guide("32").build(),
                        Question.builder().questionId(23L).keyword(myself).content("3-3").statusMap(2).guide("33").build(),
                        Question.builder().questionId(24L).keyword(myself).content("3-4").statusMap(5).guide("34").build(),
                        Question.builder().questionId(25L).keyword(myself).content("3-5").statusMap(1).guide("35").build(),
                        Question.builder().questionId(26L).keyword(myself).content("3-6").statusMap(2).guide("36").build(),
                        Question.builder().questionId(27L).keyword(myself).content("3-7").statusMap(3).guide("37").build(),
                        Question.builder().questionId(28L).keyword(myself).content("3-8").statusMap(4).guide("38").build(),
                        Question.builder().questionId(29L).keyword(myself).content("3-8").statusMap(5).guide("39").build(),
                        Question.builder().questionId(30L).keyword(myself).content("3-8").statusMap(0).guide("30").build()
        );
    }
}
