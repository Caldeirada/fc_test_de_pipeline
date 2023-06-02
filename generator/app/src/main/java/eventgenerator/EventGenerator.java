package eventgenerator;


import structures.InitEvent;
import structures.MatchEvent;
import structures.PostMatchInfoEvent;


/**
 * Class to support random event generation
 */
public abstract class EventGenerator {
    public abstract InitEvent generateInitEvent(String date);

    public abstract MatchEvent generateMatchEvent();

    public abstract PostMatchInfoEvent generatePostMatchInfoEvent();
}
