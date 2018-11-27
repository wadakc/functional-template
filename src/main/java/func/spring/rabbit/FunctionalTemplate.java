package func.spring.rabbit;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Component
public class FunctionalTemplate extends RabbitTemplate {


    private List<MethodObject> ackMethodList = new ArrayList<MethodObject>();
    private List<MethodObject> noQueueMethodList = new ArrayList<MethodObject>();

    public FunctionalTemplate(){
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost");
        connectionFactory.setPublisherConfirms(true);
        this.setConnectionFactory(connectionFactory);
        final ConfirmCallback confirmCallback = (CorrelationData correlationData, boolean ack, String cause)  -> {
            if(!ack) {
                System.err.println("not acked");
            }else {
                System.out.println("acked");
                ackMethodList.stream().forEach(methodObject -> {
                    methodObject.invoke();
                });
            }
        };
        final RabbitTemplate.ReturnCallback returnCallback = (org.springframework.amqp.core.Message message, int replyCode, String replyText, String exchange, String routingKey) ->{
            System.err.println("return exchange:" + exchange + " , routingKey:" + routingKey + ", replyCode:" + replyCode + ", replyText:" + replyText);
            noQueueMethodList.stream().forEach(methodObject -> {
                methodObject.invoke();
            });
        };
        this.setConfirmCallback(confirmCallback);
        this.setReturnCallback(returnCallback);
        this.setMandatory(true);
    }

    public void setACKMethod(Object instance, Method method, Object ... args ) {
        final MethodObject object = new MethodObject(instance,method,args);
        ackMethodList.add(object);

    }

    public void setNoQueueMethod(Object instance,Method method,Object ... args ){
        noQueueMethodList.add(new MethodObject(instance,method,args));
    }
}

