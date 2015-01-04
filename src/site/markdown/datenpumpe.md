Datenpumpe
--------------
Central logging, log aggregation and the use of logstash facilitate event sourcing. The more log events your data store holds the
more value you can gain of those events. Business related events and technical events might be produced now by your software and carried
out as log events. Log events are limited at a certain point. Sure, MDC is a good way to enrich log events with details of your runtime.
When it comes down to add real business related data (monetary amounts, durations, aggregations) you don't want to use MDC
since MDC is thread-bound and you have to clean it up afterwards. Datenpumpe is here to provide an appropriate API for
sending arbitrary data using GELF.

Datenpumpe is a german term, which means pump for data. And this is the exact purpose of it.
Use it to push any data into your (ELK) stack. Reuse GELF, reuse any Redis to push events, stats, messages and anything
for later consolidation and reporting.

Examples:

    public class MyServlet extends HttpServlet {

        public Datenpumpe datenpumpe;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)  {

            Map<String, Object> message = new HashMap<>;
            message.put("uri", req.getRequestUri());
            message.put("resource", "MyServlet");
            message.put("event", "access");

            datenpumpe.submit(message);
        }
    }

Or more sophisticated (using reflection to retrieve the fields from your model):

    @Stateless
    public class MyEjb {

        public Datenpumpe datenpumpe;

        public void shoppingCartOrdered(ShoppingCart cart) {
            datenpumpe.submit(cart);
        }
    }

    public class ShoppingCart{

        private String cartId;
        private double amount;
        private String customerId;

        public String getCartId(){
            return cartId;
        }

        public double getAmount(){
            return amount;
        }

        public String getCustomerId(){
            return customerId;
        }
    }

This results in a Gelf message like:

    { "timestamp": 1406797244.645,
      "facility": "logstash-gelf",
      "_cartId": "the cart id",
      "_amount": 9.27,
      "_customerId": "the customer id"
    }

JNDI/Resource injection is supported on JBoss AS7/Wildfly (JBoss AS8) platforms though https://github.com/mp911de/logstash-gelf-subsystem