import lombok.Getter;
import lombok.Setter;

public class SwipeDetails {
    @Getter
    @Setter
    private String swiper;

    @Getter
    @Setter
    private String swipee;

    @Getter
    @Setter
    private String comment;

    @Getter
    @Setter
    private String swipeDirection;

    public SwipeDetails(String swiper, String swipee, String comment) {
        this.swiper = swiper;
        this.swipee = swipee;
        this.comment = comment;
    }
}