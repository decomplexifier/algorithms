package decomplexified;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

/**
 * @author Alan
 *  http://decomplexify.blogspot.com/2014/05/word-ladder-ii.html
 */
public class WordLadderII {
//begin{word-adjacent}
    /**
     * helper function for testing if two words differ exactly by one letter
     */
    private boolean isAdjacent(String word1, String word2) {
        if (word1.length() != word2.length()) { return false; }
        int num_diff = 0;
        for (int i = 0; i < word1.length(); ++i) {
            if (word1.charAt(i) != word2.charAt(i)) { ++num_diff; }
            if (num_diff >= 2) { return false; }
        }
        return num_diff == 1;
    }
//end{word-adjacent}
    
//begin{word-neighbors}    
    /**
     * Helper function for iterating all words that differ only
     * by one letter from a given word. All words consist of
     * lower letters 'a' through 'z'.
     */
    private static class WordNeighbors {
        private String word0;
        private char[] newWord;
        private int index;        

        public WordNeighbors(String word) {
            word0 = word;
            newWord = word0.toCharArray();
            index = 0;
            newWord[index] = 'a' - 1;
        }
        
        /**
         * @return the next word that differs by one letter from
         * the given word. If all such words have been generated,
         * return null.
         */
        public String nextWord() {
            int n = newWord.length;
            boolean success = false;
            while (index < n && !success) {
                if (newWord[index] == 'z') {
                    index++;
                    if (index == n) { return null; }
                    newWord = word0.toCharArray();
                    newWord[index] = 'a';
                } else {
                    ++newWord[index];
                }
                success = newWord[index] != word0.charAt(index);
            }
            return success ? new String(newWord) : null;
        }     
    }
//end{word-neighbors}

//begin{find-levels}    
    /**
     * Helper function to find levels of words found by using BFS from one word
     * to the other. All intermediate words must exist in the dictionary.
     * Return null if the two words are not reachable from one to the other.
     */
    private ArrayList<HashSet<String>> findLevels(String start, String end, HashSet<String> dict) {
        ArrayList<HashSet<String>> levels = new ArrayList<>();
        
        // the first level contains only the starting word
        HashSet<String> level0 = new HashSet<>();
        level0.add(start);
        levels.add(level0);

        // in BFS, when testing if a node can be added to the new
        // level we only need to test if it has been visited in the
        // previous two levels.
        HashSet<String> currentLevel = level0;
        HashSet<String> previousLevel = null;

        // creating levels using BFS
        boolean reachedEnd = false;
        while (!reachedEnd) {
            HashSet<String> newLevel = new HashSet<>();
            for (String word : currentLevel) {
                WordNeighbors neighbors = new WordNeighbors(word);
                String next = neighbors.nextWord();
                while (next != null) {
                    if (dict.contains(next) && !currentLevel.contains(next) &&
                            (previousLevel == null || !previousLevel.contains(next))) {
                        newLevel.add(next);                        
                        if (next.equals(end)) {
                            // if the end word is reached, clear the last level and only
                            // put the end word in it
                            newLevel.clear();
                            newLevel.add(end);
                            reachedEnd = true;
                            break;
                        }
                    }
                    next = neighbors.nextWord();
                }
                if (reachedEnd) { break; }
            }
            if (newLevel.isEmpty()) { break; }

            levels.add(newLevel);
            previousLevel = currentLevel;
            currentLevel = newLevel;
        }
        return reachedEnd ? levels : null;
    }
//end{find-levels}    

//begin{trim-levels}        
    /**
     * Remove nodes that can be reached from starting word but not from the end word,
     * keep only nodes that are on some path between the two words.
     */
    private ArrayList<HashSet<String>> trimmedLevels(ArrayList<HashSet<String>> levels) {
        ArrayList<HashSet<String>> trimmed = new ArrayList<>();
        HashSet<String> previousLevel = null;
        for (HashSet<String> level : levels) {
            if (previousLevel == null) {
                previousLevel = level;
                trimmed.add(previousLevel);
                continue;
            }
            HashSet<String> newLevel = new HashSet<>();
            for (String word1 : level) {
                // keep word1 in the current level only if it's adjacent to
                // some word in the next level
                for (String word2 : previousLevel) {
                    if (isAdjacent(word1, word2)) {
                        newLevel.add(word1);
                        break;
                    }
                }
            }
            if (newLevel.isEmpty()) { break; }
            trimmed.add(newLevel);
            previousLevel = newLevel;
        }
        return trimmed;        
    }
//end{trim-levels}        

//begin{generate-paths}
    /** recursively generate all paths of words, one word from each level. Each path
     * should start at the level of index startIndex, and should be prepended with
     * partialPath. Adjacent words in any complete path should differ by exactly one
     * letter. Adjacent words in partialPath are assumed to differ by only one letter
     * already.
     */
    private void generatePaths(ArrayList<String> partialPath,
            ArrayList<HashSet<String>> levels,            
            int startIndex,
            ArrayList<ArrayList<String>> paths) {
        String lastWord = partialPath.isEmpty() ? null : partialPath.get(partialPath.size()-1);
        HashSet<String> nextLevel = levels.get(startIndex);
        for (String nextWord : nextLevel) {
            if (lastWord == null || isAdjacent(lastWord, nextWord)) {
                ArrayList<String> extendedPath = new ArrayList<>(partialPath);
                extendedPath.add(nextWord);
                if (startIndex + 1 == levels.size()) {
                    paths.add(new ArrayList<String>(extendedPath));
                } else {
                    generatePaths(extendedPath, levels, startIndex+1, paths);
                }
            }
        }
    }
//end{generate-paths}

//begin{find-ladders}
    public ArrayList<ArrayList<String>> findLadders(String start, String end, HashSet<String> dict) {
        ArrayList<ArrayList<String>> paths = new ArrayList<>();

        if (!dict.contains(start) || !dict.contains(end)) { return paths; }

        if (start.equals(end)) {
            ArrayList<String> trivial = new ArrayList<>();
            trivial.add(start);
            paths.add(trivial);
            return paths;
        }

        // find all levels using BFS backwardly
        ArrayList<HashSet<String>> levels = findLevels(end, start, dict);
        if (levels == null) { return paths; }               
        
        // trim the levels forwardly
        Collections.reverse(levels);
        levels = trimmedLevels(levels);

        ArrayList<String> partialPath = new ArrayList<>();
        generatePaths(partialPath, levels, 0, paths);
        return paths;
    }
//end{find-ladders}
    
    private static void testWordNeighbors() {
        String word = "hi";
        WordNeighbors neighbors = new WordNeighbors(word);
        System.out.print(word);
        int count = 0;
        while (true) {
            String next = neighbors.nextWord();
            if (next == null) { break; }
            count = (count + 1) % 30;
            if (count == 0) {
                System.out.println();
                System.out.print(next);
            } else {
                System.out.print(" " + next);
            }
        }
        System.out.println();
    }

    private static void testLevels() {
        WordLadderII solver = new WordLadderII();        
        String[] words = {"hit", "hot", "dot", "dog", "cog", "lot", "log", "bot", "cig", "dut"};
        HashSet<String> dict = new HashSet<String>(Arrays.asList(words));
        ArrayList<HashSet<String>> levels = solver.findLevels("cog", "hit", dict);
        for (HashSet<String> level : levels) {
            System.out.println(level);
        }
        Collections.reverse(levels);
        System.out.println("Reversely trimmed:");
        for (HashSet<String> level : solver.trimmedLevels(levels)) {
            System.out.println(level);
        }        
    }

    private static void testFindLadders() {
        WordLadderII solver = new WordLadderII();        
        String[][][] wordsList = {
          {{"hit", "cog"},{"hit", "hot", "dot", "dog", "cog", "lot", "log", "bot", "cig", "dut", "dit"}},
          {{"hot", "dog"},{"hot", "dog"}},
         {{"zings", "brown"}, {"chump","sours","mcgee","piers","match","folds","rinse","films","small","umbel","assad","morin","plied","basin","moots","blurb","suits","solve","sooty","fluky","bombs","nurse","ceres","lopes","yucky","ricks","goads","loses","coyly","marcy","bonds","niece","cures","sonic","crows","dicey","gaped","buggy","riles","homer","fakir","hello","riper","makes","laked","sinus","fangs","acton","spiky","salts","boots","skiff","maker","pence","fells","cedar","kited","raved","flake","jiffy","tanks","barns","sized","gluts","amman","jumps","cavil","quaff","rents","looms","toner","gibes","aside","drawn","karin","torte","haded","psych","hacks","jesus","fumed","lisle","spays","sumps","beats","tunas","naked","bathe","gulfs","karma","snuff","boast","grins","turds","plant","spicy","risen","tints","tomas","stand","noses","toxin","sheep","paddy","abase","jeeps","dated","tough","timid","forty","kusch","pones","smack","token","havel","vanes","repay","chums","paved","chimp","spinx","smirk","pupas","bares","mites","egged","palsy","gyros","wolfe","chips","pouts","johns","barbs","slunk","hires","seals","rally","tromp","roads","writs","aches","corny","fiats","hench","gilts","blake","phony","drams","skimp","suing","horus","hewer","barfs","hewed","needs","epsom","knots","tided","befit","eager","melva","coves","plush","pawed","zebra","gales","blots","foggy","rooks","comas","laxly","cries","kirks","monks","magic","fugue","apter","limos","congo","rosin","seder","bones","holes","fated","gamay","snags","wimpy","rites","gilds","slink","staph","sioux","bends","wilma","warts","reeds","yolks","lover","demon","salve","hulas","shard","worst","leach","omits","flint","tines","julio","trots","silly","cocks","gleam","react","camps","nicks","bored","coded","swine","scope","aloes","south","hands","rainy","david","newer","ferns","jelly","index","gibbs","truly","tubes","opera","raven","noyce","whims","titus","hared","vined","dealt","slats","erick","rolls","breed","udder","oozed","prays","tsars","harry","shelf","norms","larks","hazes","brice","gifts","units","veeps","dumas","mommy","spock","dotty","molls","slobs","diane","buddy","boost","ginny","rends","marks","timur","bands","genes","slews","leeds","karyn","mobil","mixes","ronny","sadly","rinks","smash","baled","pulpy","toils","yards","piing","dried","veils","spook","snaky","sizer","spout","percy","sheol","blank","waxes","herod","attar","doped","polls","banes","penny","knelt","laded","manic","acids","squat","jerry","stony","woofs","idles","bruin","carla","sheik","hodge","goody","merge","nicer","scums","evens","lames","wends","midge","jives","tuner","reins","boars","fryer","realm","dyson","narks","torts","yawed","waked","cress","curvy","bongs","fared","jilts","liens","ducat","shaft","pesos","dulls","donna","potty","winks","marsh","giddy","tiffs","scoot","nifty","daisy","slots","stacy","colby","skims","malls","sifts","jinns","flank","molar","hatch","wiped","taped","clink","brims","credo","fezes","molds","finds","quids","terra","damns","dusky","wanes","musty","barer","snare","honey","piked","wiser","elvin","dolly","fetal","ships","reign","cause","caved","mecca","blink","close","birth","pints","reefs","amado","comae","waite","willy","lorry","nixed","quire","napes","voted","eldon","nappy","myles","laser","pesky","leant","septa","mucks","agree","sworn","lofty","slush","holst","tevet","wases","cheer","torah","treks","purge","class","popes","roans","curve","quads","magma","drier","hales","chess","prigs","sivan","romes","finch","peels","mousy","atria","offer","coals","crash","tauts","oinks","dazed","flaps","truck","treed","colas","petty","marty","cadet","clips","zones","wooed","haves","grays","gongs","minis","macaw","horde","witch","flows","heady","fuels","conks","lifts","tumid","husks","irony","pines","glops","fonds","covey","chino","riggs","tonya","slavs","caddy","poled","blent","mired","whose","scows","forte","hikes","riped","knobs","wroth","bagel","basks","nines","scams","males","holed","solid","farms","glaxo","poise","drays","ryder","slash","rajas","goons","bowed","shirt","blurs","fussy","rills","loren","helps","feels","fiefs","hines","balms","blobs","fiord","light","dinky","maids","sagas","joked","pyxed","lilly","leers","galls","malts","minos","ionic","lower","peale","ratty","tuber","newed","whirl","eases","wests","herds","clods","floes","skate","weeds","tones","rangy","kings","adder","pitts","smith","coats","lenny","sorta","floss","looks","angie","peppy","upper","darin","white","lofts","clint","jared","heros","ruler","tonia","sexed","grail","villa","topic","kenny","dopes","hoots","boobs","gerry","eries","lyres","lunch","glove","cumin","harms","races","today","crust","track","mends","snout","shark","iliad","shrew","dorky","monty","dodge","toted","worse","dream","weird","gaunt","damon","rimes","layer","salem","bards","dills","hobby","gives","shall","crazy","brace","faxed","pools","foamy","viral","strop","liver","ceded","jolts","jonah","tight","lilia","hussy","mutts","crate","girls","marge","hypos","mewls","bulls","gazes","wands","avior","sonya","slick","clump","cater","aural","agave","grief","shana","fices","moans","grape","fetid","jenna","humus","poesy","cooks","still","lease","wanda","oddly","areas","frats","imply","files","ramon","seuss","hubby","wakes","rural","nodal","doric","carry","chefs","fails","klaus","shine","filly","yawls","brows","cabby","favor","styli","filed","jinni","ferry","balls","lakes","voled","drone","lusty","tansy","among","trail","liven","slake","madge","steps","donne","sties","picks","lacks","jumpy","meade","bogie","bauer","scene","lubes","brigs","label","fines","grebe","limns","mouse","ensue","swags","bunch","kayla","micky","sneak","bulbs","camus","yours","aisha","dunne","volta","cores","dweeb","libby","flees","shops","bided","satan","socks","draws","golfs","taunt","genus","belts","orbit","taxis","hinds","fakes","chart","wings","words","digit","copse","deena","perry","sanes","huffy","chung","lucks","fills","selma","wafts","pecks","trite","combs","sooth","weary","salty","brews","kooky","robby","loans","props","huang","marry","swabs","tinny","mince","japed","ellis","lowed","newly","loath","drown","loved","joker","lints","kinky","skits","feats","hiker","doles","every","dolby","stirs","lobed","fusty","cozen","vader","byron","dozes","slows","bethe","ploys","misty","binds","bumpy","spurs","wolfs","ernie","nails","prows","seeds","visas","dowse","pores","jocks","cower","hoofs","mined","marat","gorge","souse","clack","liter","jewel","hates","boats","stark","blabs","murks","woken","stomp","peeks","perky","pasta","goats","hocks","kinks","gushy","outdo","gelds","foxes","fives","sybil","upton","taine","helga","mauls","gills","grows","bauds","aloft","cline","payer","pinch","thorn","slits","thumb","biked","cowls","grams","disks","belly","randy","hunts","prize","minty","river","chevy","gages","cysts","years","scoff","becky","inert","abler","bevel","dyers","tonne","glows","ocean","spits","bowen","tings","baths","goals","whiny","merry","fares","leila","cairo","honor","verge","teary","pimps","sarah","meets","tamed","bumps","alias","pings","wears","dante","snore","ruled","savor","gapes","loony","chaps","froth","fancy","herbs","cutes","crowd","ghana","teddy","abate","scalp","mules","patsy","minks","shuck","billy","helen","stain","moles","jodie","homed","stack","niger","denny","kinds","elves","waled","rover","medan","churn","whizz","green","reach","lajos","mates","ditch","grads","start","press","rimed","hells","vised","slums","notes","canes","taper","camry","weans","sinks","arise","crown","prier","ramps","wotan","chars","mussy","rodes","sonar","cheri","sired","snell","basel","eider","sades","times","ovule","gusto","myrna","gabby","dully","spake","beast","towns","allay","gaged","smell","skids","clone","slack","pooch","vulva","arson","blown","kongo","maize","thick","brags","spore","soles","trial","snort","price","bowel","stoke","pents","hutch","flack","arced","cubic","hiram","tongs","lades","coons","finer","games","unpin","vests","slabs","santa","tamer","asian","tease","miked","lodes","vents","leafy","stats","shuts","bully","edith","bloch","corps","bloom","doses","coins","skips","gains","hided","coops","ninja","pills","raves","hanks","seres","ewing","bests","wrath","burgs","thrum","cabin","daren","imams","junks","brood","bacon","creel","gazed","teats","halos","gypsy","ether","train","tiles","bulks","bolls","added","roger","sites","balmy","tilts","swoop","jules","bawdy","mango","stoop","girts","costs","lemur","yucks","swazi","okays","piped","ticks","tomes","filch","depth","meals","coots","bites","pansy","spelt","leeks","hills","drops","verde","japes","holds","bangs","maxed","plume","frets","lymph","modes","twits","devon","cawed","putty","sowed","likes","quips","board","loxed","slags","dilly","refit","saved","takes","meter","prove","spacy","poach","cilia","pears","lists","gated","verdi","shave","notch","culls","shams","weedy","gaols","hoops","kraft","burro","roles","rummy","click","plots","mitty","yanks","drool","papal","rearm","prose","fucks","berra","salas","tents","flues","loves","poker","parry","polyp","agent","flown","walls","studs","troll","baron","earle","panda","wiley","raged","sexes","berne","vista","rojas","cones","byway","vases","wines","forth","freya","gully","fires","sails","dusts","terse","booed","stung","basic","saver","basis","hmong","brawn","pured","locks","downs","punts","rhine","metes","title","shims","bents","blows","harte","boyle","peach","posts","olson","might","flier","rubes","lingo","tarts","nexus","woman","mains","finis","mikes","pleas","trams","shawl","gunny","sleds","ruder","aries","usher","refed","toady","caper","tries","gimpy","doors","thieu","deere","mucky","rests","mares","cards","bouts","dines","rants","giles","flunk","enact","derek","dover","conan","mooed","fiver","kaput","enrol","payed","feint","miner","shyer","whelk","perch","furor","hayes","tammy","caves","maims","cairn","tract","legal","adler","veldt","basal","spiny","surer","bolds","grove","heaps","noway","pokes","tubed","beaks","loots","drawl","jones","typed","funny","cells","beaus","bayed","rears","seats","hazed","flubs","maura","goths","rumba","morse","fumes","slide","snoot","music","sully","perth","pocks","mills","lopez","sacks","stine","gawks","gavel","rains","wound","hares","guild","leger","foxed","craws","rinds","faced","groom","lully","boded","lends","serge","sword","faked","envoy","stick","tumor","riser","bolts","trued","gasps","thoth","veers","verbs","boles","lunar","taxes","vexes","pucks","welsh","pelts","shift","booth","smote","spied","gnawn","crete","dough","tasha","timed","wired","state","hears","lauds","wills","dummy","basil","belie","calls","crams","matts","gybes","limed","snots","moder","faces","sibyl","spare","crops","drips","frown","doggy","pearl","reese","curls","earns","poles","tiara","risks","lethe","titan","tucks","trace","vises","prick","sears","ogled","preps","livid","kicky","candy","weeps","tapes","cokes","foods","wards","coifs","shirk","elsie","ketch","trunk","goofs","kodak","toyed","lance","whale","soups","roars","poxed","tombs","noons","hindi","basie","hoffa","bayou","tests","roots","shove","hoses","doled","tempt","kilos","velma","avers","dorks","comic","fanny","poops","sicks","leary","merer","finks","garbo","cains","mimed","sates","celli","flats","grown","broth","augur","chaos","sangs","chide","barks","guide","mewed","synch","rings","scrap","zings","howls","duded","noemi","geeks","nexis","comte","helot","whams","brand","hogan","moira","trips","loges","baits","winds","marla","never","louis","anted","helix","morns","heeds","crags","rowdy","becks","venue","diary","stoat","feeds","kiths","riled","drags","lucia","deeps","sends","fonts","swing","fence","stout","trice","taker","drugs","babel","plows","pends","sloes","gents","brawl","arabs","leaps","flied","fulls","meats","megan","burch","oscar","evict","betsy","lasts","ethos","mavis","petal","fever","alone","snips","assay","rocks","talon","grass","clive","discs","wrapt","calfs","razed","learn","bruce","midst","swear","merck","meyer","funks","lobby","fears","decay","sedge","alien","reaps","koran","range","enter","lepke","honed","gallo","staid","joist","lines","paler","fined","sorts","piper","highs","busch","dario","north","ashed","sands","songs","rakes","garza","pinks","rival","leann","allow","golds","hilts","berry","hicks","idler","weiss","cider","desks","skies","hulls","warns","datum","brown","leapt","dregs","dozed","stump","reply","finny","clues","diode","dicks","rabid","moors","limbs","gulls","scary","dungs","liege","vicky","nigel","peeps","dolls","blame","sings","wants","fuzes","proud","bungs","seams","bingo","buffs","shire","decks","hosed","scots","pumas","jazzy","books","ellie","hayed","snowy","twill","links","coped","spats","reyes","piles","hovel","reads","wryer","patty","sling","oneal","waves","gorse","ofter","teams","strep","mores","daily","spoil","limes","foots","dells","hakes","danny","furls","flaws","tarot","dusty","potts","tells","pager","claps","serra","josie","award","pewee","snack","lobes","damps","tanya","lures","mushy","hertz","caret","marco","parks","pithy","synge","spoon","troth","drama","bleak","lidia","banns","forms","iambs","crick","patel","mercy","waded"}}
        };
        for (String[][] endsAndWords : wordsList) {
            System.out.println("------------");
            String start = endsAndWords[0][0];
            String end = endsAndWords[0][1];            
            HashSet<String> dict = new HashSet<String>(Arrays.asList(endsAndWords[1]));
            ArrayList<ArrayList<String>> paths = solver.findLadders(start, end, dict);

            for (ArrayList<String> path : paths) {
                System.out.println(path);
            }
        }
    }
    
    public static void main(String[] args) {
        testWordNeighbors();
        testLevels();
        testFindLadders();
    }

}