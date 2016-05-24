import actors.QueueListenerActor;
import com.google.inject.AbstractModule;
import play.libs.akka.AkkaGuiceSupport;


public class Module extends AbstractModule implements AkkaGuiceSupport {

    @Override
    public void configure() {
        bindActor(QueueListenerActor.class, "queueListenerActor");
    }
}
