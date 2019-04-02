package biz.paluch.logging.gelf.standalone;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch
 */
class DatenpumpeImplUnitTests {

    @BeforeEach
    void before() throws Exception {
        GelfTestSender.getMessages().clear();
    }

    @Test
    void testBean() throws Exception {
        MyBean bean = new MyBean();

        DefaultGelfSenderConfiguration configuration = new DefaultGelfSenderConfiguration();
        configuration.setHost("test:static");

        DatenpumpeImpl datenpumpe = new DatenpumpeImpl(configuration);

        datenpumpe.submit(bean);

        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getField("value")).isEqualTo("value field");
        assertThat(gelfMessage.getField("boolean")).isEqualTo("true");
        assertThat(gelfMessage.getField("object")).isNotNull();

        assertThat(gelfMessage.getAdditonalFields()).hasSize(3);

        datenpumpe.close();

        // additional check for NPE
        datenpumpe.close();

    }

    @Test
    void testShoppingCart() throws Exception {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setCartId("the cart id");
        shoppingCart.setAmount(9.27);
        shoppingCart.setCustomerId("the customer id");

        DefaultGelfSenderConfiguration configuration = new DefaultGelfSenderConfiguration();
        configuration.setHost("test:static");

        DatenpumpeImpl datenpumpe = new DatenpumpeImpl(configuration);

        datenpumpe.submit(shoppingCart);

        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        System.out.println(gelfMessage.toJson());

    }
}
