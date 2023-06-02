package eventgenerator;


import structures.InAppPurchase;
import structures.InitEvent;
import structures.MatchEvent;
import structures.PostMatchInfoEvent;


/**
 * Class to support random event generation
 */
public abstract class EventGenerator {
    public abstract InitEvent generateInitEvent(String date);

    public abstract MatchEvent generateMatchEvent(String date, InitEvent user_a, InitEvent user_b);

    public abstract PostMatchInfoEvent generatePostMatchInfoEvent(InitEvent user);

    public abstract InAppPurchase generateIAPEvent(String date, InitEvent user);
}
