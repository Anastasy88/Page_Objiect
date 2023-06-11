package ru.netology.web.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.netology.web.data.DataHelper;
import ru.netology.web.page.DashboardPage;
import ru.netology.web.page.LoginPage;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.netology.web.data.DataHelper.*;

class MoneyTransferTest {
    LoginPage LoginPage;
    DashboardPage dashboardPage;

    @BeforeEach
    void setup() {
        var loginPage = open("http://localhost:9999", LoginPage.class);  //открыть сайт
        var authInfo = DataHelper.getAuthInfo();  //получить данные аутентификации
        var verificationPage = loginPage.validLogin(authInfo); //переход на стр.варификации
        var verificationCode = DataHelper.getVerificationCode(); //получение кода варификации
        dashboardPage = verificationPage.validVerify(verificationCode); //стр.дашборда (пополнение баланса)
    }

    @Test
    void shouldTransferFromFirstToSecond() {
        var firstCardInfo = getFirstCardInfo(); //данные карт
        var secondCardInfo = getSecondCardInfo();  //данные карт
        var firstCardBalance = dashboardPage.getCardBalance(firstCardInfo); //валидная сумма перевода
        var secondCardBalance = dashboardPage.getCardBalance(secondCardInfo);
        var amount = creatValidAmount(firstCardBalance);
        var expectedBalanceFirstCard = firstCardBalance - amount; // ожидаемые суммы после перевода
        var expectedBalanceSecondCard = secondCardBalance + amount;
        var transactionPage = dashboardPage.selectCardToTransfer(secondCardInfo); //выполнение перевода
        dashboardPage = transactionPage.makeValidTransaction(String.valueOf(amount), firstCardInfo);
        var actualBalanceFirstCard = dashboardPage.getCardBalance(firstCardInfo); //фактические балансы
        var actualBalanceSecondCard = dashboardPage.getCardBalance(secondCardInfo);//фактические балансы
        assertEquals(expectedBalanceFirstCard, actualBalanceFirstCard);
        assertEquals(expectedBalanceSecondCard, actualBalanceSecondCard);
    }

    @Test
    void shouldGetErrorMessageIfAmountMoreBalance() {
        var firstCardInfo = getFirstCardInfo();
        var secondCardInfo = getSecondCardInfo();
        var firstCardBalance = dashboardPage.getCardBalance(firstCardInfo);
        var secondCardBalance = dashboardPage.getCardBalance(secondCardInfo);
        var amount = creatInvalidAmount(secondCardBalance);
        var transactionPage = dashboardPage.selectCardToTransfer(firstCardInfo);
        transactionPage.makeTransaction(String.valueOf(amount), secondCardInfo);
        transactionPage.findErrorMessage("Выполнена попытка перевода суммы, превышающей остаток на карте списания");
        var actualBalanceFirstCard = dashboardPage.getCardBalance(firstCardInfo);
        var actualBalanceSecondCard = dashboardPage.getCardBalance(secondCardInfo);
        assertEquals(firstCardBalance, actualBalanceFirstCard);
        assertEquals(secondCardBalance, actualBalanceSecondCard);
    }
}