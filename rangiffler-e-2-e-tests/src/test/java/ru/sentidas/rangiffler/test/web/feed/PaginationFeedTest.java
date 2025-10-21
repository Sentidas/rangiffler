package ru.sentidas.rangiffler.test.web.feed;

import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.jupiter.annotaion.ApiLogin;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.WebTest;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.model.CountryName;
import ru.sentidas.rangiffler.page.FeedPage;
import ru.sentidas.rangiffler.page.component.Pagination;
import ru.sentidas.rangiffler.service.PhotoDbClient;
import ru.sentidas.rangiffler.utils.AnnotationHelper;

import java.util.UUID;

import static ru.sentidas.rangiffler.page.component.Pagination.Next;
import static ru.sentidas.rangiffler.page.component.Pagination.Prev;
import static ru.sentidas.rangiffler.utils.AnnotationHelper.firstFriendId;
import static ru.sentidas.rangiffler.utils.AnnotationHelper.firstPhotoId;

@WebTest
public class PaginationFeedTest {

    private final PhotoDbClient photo = new PhotoDbClient();

    private static final String TEST_IMAGE_PATH = "photo/4.png";
    private static final String TEST_COUNTRY = CountryName.labelByCode("do"); // Dominican Republic
    private static final String TEST_DESCRIPTION = "я наконец тут";

    @Test
    @User
    @ApiLogin
    @DisplayName("Мои фото: 0→1 — после добавления 1-го фото пагинация появляется, обе кнопки неактивны")
    void paginationShouldAppearAndButtonsDisabledWhenPhotoAddedOnMyFeed() {
        new FeedPage()
                .shouldHaveNoPagination()
                .addPhoto(
                        TEST_IMAGE_PATH,
                        TEST_COUNTRY,
                        TEST_DESCRIPTION)
                .shouldHavePagination(Prev.DISABLE, Next.DISABLE);
    }

    @Test
    @User(friends = 1)
    @ApiLogin
    @DisplayName("Фото с друзьями: 0→1 — после добавления 1-го фото пагинация появляется, обе кнопки неактивны")
    void paginationShouldAppearAndButtonsDisabledWhenPhotoAddedOnFriendsFeed() {
        new FeedPage()
                .openFriendsFeed()
                .shouldHaveNoPagination()
                .addPhoto(
                        TEST_IMAGE_PATH,
                        TEST_COUNTRY,
                        TEST_DESCRIPTION)
                .shouldHavePagination(Prev.DISABLE, Next.DISABLE);
    }

    @Test
    @User(photo = 11)
    @ApiLogin
    @DisplayName("Мои фото: 11→12 — при добавлении 12-го фото пагинация остаётся неактивной (в пределах одной страницы)")
    void paginationShouldStayDisabledWhenAddingTwelfthPhotoOnMyFeed() {
        new FeedPage()
                .shouldHavePagination(Prev.DISABLE, Next.DISABLE)
                .addPhoto(
                        TEST_IMAGE_PATH,
                        TEST_COUNTRY,
                        TEST_DESCRIPTION)
                .shouldHavePagination(Prev.DISABLE, Next.DISABLE);
    }

    @Test
    @User(photo = 10, friends = 1, friendsWithPhotosEach = 1)
    @ApiLogin
    @DisplayName("Фото с друзьями: 11→12 — при добавлении 12-го фото пагинация остаётся неактивной (в пределах одной страницы)")
    void paginationShouldStayDisabledWhenAddingTwelfthPhotoOnFriendsFeed(AppUser user) {
        new FeedPage()
                .openFriendsFeed()
                .shouldHavePagination(Prev.DISABLE, Next.DISABLE)
                .addPhoto(
                        TEST_IMAGE_PATH,
                        TEST_COUNTRY,
                        TEST_DESCRIPTION)
                .shouldHavePagination(Prev.DISABLE, Next.DISABLE);
    }

    @Test
    @User(photo = 12)
    @ApiLogin
    @DisplayName("Мои фото: 12→13 — 'Next' становится активна при появлении 2-й страницы")
    void nextShouldBeEnabledWhenSecondPageAppearsOnMyFeed() {
        new FeedPage()
                .shouldHavePagination(Prev.DISABLE, Next.DISABLE)
                .addPhoto(
                        TEST_IMAGE_PATH,
                        TEST_COUNTRY,
                        TEST_DESCRIPTION)
                .shouldHavePagination(Prev.DISABLE, Next.ENABLE);
    }

    @Test
    @User(photo = 12)
    @ApiLogin
    @DisplayName("Фото с друзьями: 12→13 — — 'Next' становится активна при появлении 2-й страницы")
    void nextShouldBeEnabledWhenSecondPageAppearsOnFriendsFeed() {
        new FeedPage()
                .openFriendsFeed()
                .shouldHavePagination(Prev.DISABLE, Next.DISABLE)
                .addPhoto(
                        TEST_IMAGE_PATH,
                        TEST_COUNTRY,
                        TEST_DESCRIPTION)
                .shouldHavePagination(Prev.DISABLE, Next.ENABLE);
    }


    @Test
    @User(photo = 24)
    @ApiLogin
    @DisplayName("Мои фото: 24→25 — 'Next' становится активна на 2-й странице при появлении 3-й страницы")
    void nextShouldBeEnabledOnSecondPageWhenThirdPageAppearsOnMyFeed() {
        FeedPage feedPage = new FeedPage().shouldHavePagination(Prev.DISABLE, Next.ENABLE);
        feedPage.pagination().clickNext();

        feedPage.shouldHavePagination(Prev.ENABLE, Next.DISABLE)
                .addPhoto(
                        TEST_IMAGE_PATH,
                        TEST_COUNTRY,
                        TEST_DESCRIPTION)
                .shouldHavePagination(Prev.ENABLE, Next.ENABLE);
    }

    @Test
    @User(photo = 24)
    @ApiLogin
    @DisplayName("Фото с друзьями: 24→25 — 'Next' становится активна на 2-й странице при появлении 3-й страницы")
    void nextShouldBeEnabledOnSecondPageWhenThirdPageAppearsOnFriendsFeed() {
        FeedPage feedPage = new FeedPage()
                .openFriendsFeed()
                .shouldHavePagination(Prev.DISABLE, Next.ENABLE);
        feedPage.pagination().clickNext();

        feedPage.shouldHavePagination(Prev.ENABLE, Next.DISABLE)
                .addPhoto(
                        TEST_IMAGE_PATH,
                        TEST_COUNTRY,
                        TEST_DESCRIPTION)
                .shouldHavePagination(Prev.ENABLE, Next.ENABLE);
    }

    @Test
    @User(photo = 13)
    @ApiLogin
    @DisplayName("Мои фото: стр.1 — 'Prev' off, 'Next' on; стр.2 — 'Prev' on, 'Next' off")
    void prevEnabledAndNextDisabledAfterNavigatingToSecondPageOnMyFeed(AppUser user) {
        System.out.println(user.username());

        FeedPage feedPage = new FeedPage()
                .shouldHavePagination(Prev.DISABLE, Next.ENABLE);

        feedPage.pagination().clickNext();

        feedPage.shouldHavePagination(Prev.ENABLE, Next.DISABLE);
    }

    @Test
    @User(photo = 13)
    @ApiLogin
    @DisplayName("Фото с друзьями: стр.1 — 'Prev' off, 'Next' on; стр.2 — 'Prev' on, 'Next' off")
    void prevEnabledAndNextDisabledAfterNavigatingToSecondPageOnFriendsFeed() {
        FeedPage feedPage = new FeedPage()
                .openFriendsFeed()
                .shouldHavePagination(Prev.DISABLE, Next.ENABLE);

        feedPage.pagination().clickNext();
        feedPage.shouldHavePagination(Prev.ENABLE, Next.DISABLE);
    }

    @Test
    @User(photo = 13)
    @ApiLogin
    @DisplayName("Мои фото: стр.2 — 'Prev' on, 'Next' off; стр.1 — 'Prev' off, 'Next' on")
    void previousBecomesDisabledAndNextEnabledAfterBackToFirstPageOnMyFeed() {
        FeedPage feedPage = new FeedPage();
        Pagination pagination = feedPage.pagination().clickNext();

        feedPage.shouldHavePagination(Prev.ENABLE, Next.DISABLE);
        pagination.clickPrevious();
        feedPage.shouldHavePagination(Prev.DISABLE, Next.ENABLE);
    }

    @Test
    @User(photo = 13)
    @ApiLogin
    @DisplayName("Фото с друзьями: стр.2 — 'Prev' on, 'Next' off; стр.1 — 'Prev' off, 'Next' on")
    void previousBecomesDisabledAndNextEnabledAfterBackToFirstPageOnFriendsFeed() {
        FeedPage feedPage = new FeedPage()
                .openFriendsFeed();

        Pagination pagination = feedPage.pagination().clickNext();
        feedPage.shouldHavePagination(Prev.ENABLE, Next.DISABLE);
        pagination.clickPrevious();
        feedPage.shouldHavePagination(Prev.DISABLE, Next.ENABLE);
    }

    @Test
    @User(photo = 13)
    @ApiLogin
    @DisplayName("Мои фото: удаление единственного фото на 2-й странице → одна страница, обе кнопки неактивны")
    void paginationBecomesDisabledAfterDeletingOnlyPhotoOnSecondPageOnMyFeed() {
        FeedPage feedPage = new FeedPage()
                .shouldHavePagination(Prev.DISABLE, Next.ENABLE); // 13 фото: со 1-й стр. можно идти вперёд

        feedPage.pagination().clickNext();
        feedPage.shouldHavePagination(Prev.ENABLE, Next.DISABLE); // на 2-й «Next» off

        feedPage.deletePhotoByNumber(1);
        feedPage.shouldHavePagination(Prev.DISABLE, Next.DISABLE); // стало 12, одна страница
    }

    @Test
    @User(photo = 12, friends = 1, friendsWithPhotosEach = 1)
    @ApiLogin
    @DisplayName("Мои фото: удаление единственного фото на 2-й странице → одна страница, обе кнопки неактивны")
    void paginationBecomesDisabledAfterDeletingOnlyPhotoOnSecondPageOnFriendsFeed() {
        FeedPage feedPage = new FeedPage()
                .openFriendsFeed()
                .shouldHavePagination(Prev.DISABLE, Next.ENABLE); // 13 фото: со 1-й стр. можно идти вперёд

        feedPage.pagination().clickNext();
        feedPage.shouldHavePagination(Prev.ENABLE, Next.DISABLE); // на 2-й «Next» off

        feedPage.deletePhotoByNumber(1);
        feedPage.shouldHavePagination(Prev.DISABLE, Next.DISABLE); // стало 12, одна страница
    }

    @Test
    @User(photo = 12, friends = 1, friendsWithPhotosEach = 1)
    @ApiLogin
    @DisplayName("Фото с друзьями: кнопка 'Next' должна стать активной только на 'Фото с друзьями' при наличии фото у друга")
    void nextEnabledOnlyWhenFriendHasPhotosOnFriendsFeed() {
        FeedPage feedPage = new FeedPage()
                .shouldHavePagination(Prev.DISABLE, Next.DISABLE)
                .openFriendsFeed()
                .shouldHavePagination(Prev.DISABLE, Next.ENABLE);
        feedPage.pagination().clickNext();
        feedPage.shouldHavePagination(Prev.ENABLE, Next.DISABLE);
    }

    @Test
    @User(photo = 6, friends = 1, friendsWithPhotosEach = 1)
    @ApiLogin
    @DisplayName("Клики по неактивным кнопкам пагинации не должны менять страницу (Мои/Друзья)")
    void disabledButtonsShouldNotChangePage_onMyAndFriends() {
        FeedPage feedPage = new FeedPage();
        feedPage.shouldHavePagination(Prev.DISABLE, Next.DISABLE);
        feedPage.pagination().tryClickNext();
        feedPage.shouldHavePagination(Prev.DISABLE, Next.DISABLE);
        feedPage.pagination().tryClickPrevious();
        feedPage.shouldHavePagination(Prev.DISABLE, Next.DISABLE);

        feedPage.openFriendsFeed();
        feedPage.shouldHavePagination(Prev.DISABLE, Next.DISABLE);
        feedPage.pagination().tryClickNext();
        feedPage.shouldHavePagination(Prev.DISABLE, Next.DISABLE);
        feedPage.pagination().tryClickPrevious();
        feedPage.shouldHavePagination(Prev.DISABLE, Next.DISABLE);
    }

    @Test
    @User(photo = 12, friends = 1, friendsWithPhotosEach = 1)
    @ApiLogin
    @DisplayName("Фото с друзьями: после удаления фото друга 'Next' становится недоступной")
    void friendsPaginationUpdatesAfterFriendPhotoDeleted(AppUser user) {
        UUID photoId = firstPhotoId(user, firstFriendId(user));

        FeedPage feedPage = new FeedPage();
        feedPage.openFriendsFeed()
                .shouldHavePagination(Prev.DISABLE, Next.ENABLE);

        photo.removePhotoById(photoId);
        Selenide.refresh();

        feedPage.openFriendsFeed()
                .shouldHavePagination(Prev.DISABLE, Next.DISABLE);
    }
}
