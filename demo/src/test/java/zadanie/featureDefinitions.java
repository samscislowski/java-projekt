package zadanie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.support.ui.Select;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * step1
 */
public class featureDefinitions {


    String driverPath = System.getProperty("user.dir") + "\\msedgedriver.exe";

    WebDriver driver = null;

    @Before                                             //Funkcja uruchamia sie przed każdym scenario inijcuje drivera
    public void initDriver() {
        System.setProperty("webdriver.edge.driver", driverPath);

        driver = new EdgeDriver(); // inicjacja drivera
        driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
        driver.manage().timeouts().pageLoadTimeout(40, TimeUnit.SECONDS);
    }

    @After                                              //Funkcja uruchamia sie po kazdym scenario zamyka drivera
    public void disposeDriver() {

        driver.close();
    }

    @Given("nawiguje do:")                              //Fukncja do wiekokrotnego uzytku przez podanie wartości w pliku feature jako DataTable: https://www.baeldung.com/cucumber-data-tables
    public void navigateTo(DataTable table) {
        String url = table.cell(0, 0).toString();
        driver.navigate().to(url);
    }

    @Then("powinienem byc na stronie profilu")
    public void isNavigatedToProfile() {

        assertEquals(driver.getCurrentUrl(), "https://prod-kurs.coderslab.pl/index.php?controller=my-account");
    }

    @And("wpisuje login i haslo")
    public void writeLoginAndPassword() {
        driver.findElement(By.name("email")).sendKeys("cwzvohbyxxnsfgsdlc@ianvvn.com");
        driver.findElement(By.name("password")).sendKeys("password");

    }

    @And("klikam zaloguj")
    public void clickLoginButton() {
        driver.findElement(By.id("submit-login")).click();

    }

    @And("klikam zakladke adreses")
    public void clickAdressesBookmark() {

        List<WebElement> elements = driver.findElements(By.id("addresses-link"));   //sprawdza czy jest element addresses (on występuje kiedy jest juz pierwszy adres podany)
        if (elements.size() > 0) {
            elements.get(0).click();                                                //jezeli jest klika go
        } else {
            elements = driver.findElements(By.id("address-link"));                  //sprawdza czy jest element address (on występuje kiedy nie ma pierwszego adresu podanego)
            if (elements.size() > 0) {
                elements.get(0).click();                                            //jezeli jest klika go
            } else {
                throw new NoSuchElementException("element not found");              //jak nie ma to err ale nie ma takiego przypadku o ile jest na wlasciwej stronie
            }
        }

    }

    @And("klikam zakladke adres")
    public void clickAdressBookmark() {
        List<WebElement> elements = driver.findElements(By.id("address-link"));
        if (elements.size() > 0) {
            elements.get(0).click();
        }
    }

    @And("klikam dodaj adres")
    public void clickAddAdress() {
        String x = driver.getCurrentUrl();
        if (!x.equals("https://prod-kurs.coderslab.pl/index.php?controller=address")) { //jezeli url rózny niż address (strona przekierowyje odrazu na to do dodawania adresu jak nie byl podany adres wczesniej), zeby pomineło clikanie add adress
            driver.findElement(By.partialLinkText("Create new address")).click();

        }

    }


    @When("^uzupelniam dane (.*) and (.*) and (.*) and (.*) and (.*) and (.*)$")
    public void insertAdress(String alias, String address, String city, String postal, String country, String phone) {
        driver.findElement(By.name("alias")).sendKeys(alias);
        driver.findElement(By.name("address1")).sendKeys(address);
        driver.findElement(By.name("city")).sendKeys(city);
        driver.findElement(By.name("postcode")).sendKeys(postal);
        Select countrySelect = new Select(driver.findElement(By.name("id_country"))); // specjalna skladnia do ustawiana kontrolek typu select
        countrySelect.selectByVisibleText(country);                                   //zaznacza opcje z nazwa...
        driver.findElement(By.name("phone")).sendKeys(phone);
    }

    @And("klikam save")
    public void clickSave() {
        driver.findElement(By.xpath("//*[contains(text(), 'Save')]")).click();        //xpath zeby zaznaczylo element przez text elementu np <a>Save</a>: https://testelka.pl/xpath/
    }

    @Then("powinno sie zapisac")
    public void checkIfSaved() {
        assertEquals(driver.getCurrentUrl(), "https://prod-kurs.coderslab.pl/index.php?controller=addresses"); //jak nie bedzie przekierowania na ten adres to nie zapisalo
    }

    @When("^usuwam adres (.*)$")
    public void deleteAdress(String alias) {
        List<WebElement> elementsToDelete = new ArrayList<WebElement>();          //pusta lista na elementy do późniejszego usunięcia bez tego byłby błąd nullreference przy modyfikacji "na zywo" (referencja do usunietego/nie istniejacego)
        List<WebElement> elements = driver.findElements(By.className("address")); //pierw zbiera wszystkie "kontenery" z adresami
        if (elements.size() > 0) {
            for (WebElement item : elements) {
                if (item.findElement(By.className("address-body")).findElement(By.tagName("h4")).getText().equals(alias)) {
                    elementsToDelete.add(item.findElement(By.className("address-footer")).findElement(By.partialLinkText("Delete"))); //jak instnieje taki alias to doda go do listy do usuniecia
                }

            }
        }
        for (WebElement item : elementsToDelete) {
            item.click();                                       //klika usuń przy odpowiednich elementach :v
        }
    }

    @Then("^powinien byc usuniety (.*)$")
    public void isDeleted(String alias) {
        if (!driver.getCurrentUrl().equals("https://prod-kurs.coderslab.pl/index.php?controller=addresses")) {
            driver.navigate().to("https://prod-kurs.coderslab.pl/index.php?controller=addresses");          //jak nie jest na właściwej stronie przekierowuje na nią (przypadek gdzie nie ma zadnych adresow strona przekierowuje cie na inna niz powinno)
        }
        List<WebElement> elements = driver.findElements(By.className("address"));
        if (elements.size() > 0) {
            for (WebElement item : elements) {
                if (item.findElement(By.className("address-body")).findElement(By.tagName("h4")).getText().equals(alias)) {
                    fail();                                             //jak jednak znajdzie alias w adresach err
                }

            }
        }
    }


    @And("czy ma przecene")
    public void hasDiscount(DataTable table) {
        if (!driver.findElement(By.className("discount-percentage")).getText().contains(table.cell(0, 0))) {
            fail();                                                    //jak nie bedzie przeceny podanej z argumentu to err (nie bylo wymagane z parametrem ale juz zrobilem)
        }
    }

    @And("^wybiera rozmiar (.*)$")
    public void setSize(String size) {
        Select countrySelect = new Select(driver.findElement(By.id("group_1")));
        countrySelect.selectByVisibleText(size);
    }

    @And("^wybiera ilosc (.*)$")
    public void setAmount(String amount) {
        WebElement x = driver.findElement(By.id("quantity_wanted"));
        x.clear();                          //przed wpisaniem czyści wartość żeby nie bylo przy podaniu 4 wynik 14
        x.sendKeys(amount);
    }

    @And("dodaje do koszyka")
    public void addToCart() {

        driver.findElement(By.className("add-to-cart")).click();
    }

    @And("kliknij opcje checkout")
    public void clickCheckout() {
        driver.findElement(By.cssSelector("div[class$='cart-content-btn']")).findElement(By.cssSelector("a[class$='btn btn-primary']")).click();    //tricky rzecz bo elementy doddawane dynamicznie na stronie są poza DOM (driver nie wylapie metoda By.id()) ale za to można zrobic w ten sposob
    }

    @And("potwierdza kupno")
    public void proceedCheckout() {

        driver.findElement(By.className("cart-detailed-actions")).findElement(By.tagName("a")).click();
    }

    @And("^wybiera i potwierdza adres (.*)$")
    public void chooseAdress(String alias) {
        List<WebElement> elements = driver.findElements(By.className("address-item"));  //zbieranie adresow
        if (elements.size() > 0) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            for (WebElement item : elements) {                                          //sprawdza w kazdym z adresow czy ma alias jak ma to zaznacza przez skypt js radio buttona i dodaje/usuwa klase css 'seleceted' w 'wybiera i potwierdza platnosc' zrobilem to w prostszy sposob
                if (item.findElement(By.tagName("header")).findElement(By.tagName("label")).findElement(By.className("address-alias")).getText().equals(alias)) {
                    js.executeScript("arguments[0].checked = true", item.findElement(By.className("custom-radio")).findElement(By.name("id_address_delivery")));
                    js.executeScript("arguments[0].classList.add('selected')", item);

                } else {


                    js.executeScript("arguments[0].checked = false", item.findElement(By.className("custom-radio")).findElement(By.name("id_address_delivery")));
                    js.executeScript("arguments[0].classList.remove('selected')", item);

                }

            }
        } else {
            fail();                                     // jak nie ma zadnego adresu fail
        }
        driver.findElement(By.name("confirm-addresses")).click();
    }

    @And("wybiera i potwierdza dostawe")
    public void chooseDelivery() {

        driver.findElement(By.name("confirmDeliveryOption")).click();
    }

    @When("wybiera i potwierdza platnosc")
    public void choosePayment() {
        //JavascriptExecutor js = (JavascriptExecutor) driver;
        //js.executeScript("arguments[0].checked = true", driver.findElement(By.id("payment-option-1")));
        //js.executeScript("arguments[0].checked = true", driver.findElement(By.id("conditions_to_approve[terms-and-conditions]"))); //to nie triggerowalo odblokowania opcji ukonczenia zamowienia i mozna bylo prosciej na co w koncu wpadlem

        driver.findElement(By.id("payment-option-1")).click();
        driver.findElement(By.className("custom-checkbox")).click();

        driver.findElement(By.id("payment-confirmation")).findElement(By.tagName("button")).click();
    }

    @Then("zrzut ekranu zamowienia")
    public void screenshot() throws Exception{
        TakesScreenshot scrShot = ((TakesScreenshot) driver);

        try (FileOutputStream stream = new FileOutputStream("screen.png")) {    //tworzy pusty plik a potem wpisuje dane w plik (bedzie za kazdym wywolaniem go nadpisywac)
            stream.write(scrShot.getScreenshotAs(OutputType.BYTES));
        }
    }

}
