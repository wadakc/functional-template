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


    private final List<MethodObject> ackMethodList = new ArrayList<MethodObject>();
    private final List<MethodObject> notAckMethodList = new ArrayList<MethodObject>();
    private final List<MethodObject> noQueueMethodList = new ArrayList<MethodObject>();

    private String hostName;

    public FunctionalTemplate(){
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost");
        connectionFactory.setPublisherConfirms(true);
        this.setConnectionFactory(connectionFactory);
        final ConfirmCallback confirmCallback = (CorrelationData correlationData, boolean ack, String cause)  -> {
            if(!ack) {
                notAckMethodList.stream().forEach(MethodObject::invoke);
            }else {
                ackMethodList.stream().forEach(MethodObject::invoke);
            }
        };
        final RabbitTemplate.ReturnCallback returnCallback = (org.springframework.amqp.core.Message message, int replyCode, String replyText, String exchange, String routingKey) ->{
            noQueueMethodList.stream().forEach(MethodObject::invoke);
        };
        this.setConfirmCallback(confirmCallback);
        this.setReturnCallback(returnCallback);
        this.setMandatory(true);
    }

    public void setOnAckMethod(Object instance, Method method, Object ... args ) {
        ackMethodList.add(methodObject(instance,method,args));
    }

    public void setNoAckedMethod(Object instance, Method method, Object ... args){
        notAckMethodList.add(methodObject(instance,method,args));
    }

    public void setNoQueueMethod(Object instance,Method method,Object ... args ){
        noQueueMethodList.add(methodObject(instance,method,args));
    }


    public static MethodObject methodObject(Object instance , Method method, Object ... args){
        return new MethodObject(instance,method,args);
    }
}

