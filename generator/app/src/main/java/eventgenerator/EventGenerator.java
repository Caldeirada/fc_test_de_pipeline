package eventgenerator;


import structures.InAppPurchase;
import structures.InitEvent;
import structures.MatchEvent;
import structures.PostMatchInfoEvent;


/**
 * Class to support random event generation
 */
public abstract class EventGenerator {

    public abstract InitEvent generateInitEvent();

    public abstract MatchEvent generateMatchEvent(InitEvent user_a, InitEvent user_b);

    public abstract PostMatchInfoEvent generatePostMatchInfoEvent(InitEvent user);

    public abstract InAppPurchase generateIAPEvent(InitEvent user);
}
