/* Copyright Rupert Smith, 2005 to 2008, all rights reserved. */
package com.thesett.index;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.NDC;

import com.thesett.index.prototype.ProtoIndex;
import com.thesett.index.tx.IndexTxId;
import com.thesett.index.tx.IndexTxManager;
import com.thesett.junit.concurrency.TestRunnable;
import com.thesett.junit.concurrency.ThreadTestCoordinator;
import com.thesett.junit.extensions.AsymptoticTestCase;

/**
 * Does performance and stress testing on transactional indexes. Performance testing is designed around typical
 * usage of an index; lots of reads and occasional single updates to ratings. Stress testing is based around concurrent
 * reads, updates and uploads all mixed together and not modeled on typical usage.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Test typical index usage under varying loads.
 * <tr><td> Test index reliability under heavy loading.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class TransactionalIndexPerfTestBase extends AsymptoticTestCase
{
    /** Used for logging. */
    //private static final Logger log = Logger.getLogger(TransactionalIndexPerfTestBase.class);

    /** Sets the deadlock timeout to break blocked multi-thread interactions after. In milliseconds. */
    private static final int DEADLOCK_TIMEOUT = 5000;

    /** Sequence numbers used to make data unique accross many threads running the tests at the same time. */
    static AtomicLong sequence = new AtomicLong();

    /** Random number generator. */
    static Random random = new Random();

    /** The probability of a write transactions to mimic typical index usage. */
    float WRITE_TRANSACTION_RATIO = 0.1f;

    /** Index size to test. */
    int INDEX_SIZE = 100;

    /** Text size in words of records in the index. */
    int TEXT_SIZE = 50;

    /** Holds the test records. */
    TestRecord[] records = new TestRecord[INDEX_SIZE];

    /** The {@link TransactionalIndex} to test.*/
    TransactionalIndex testIndex;

    /** The test indexes setup instance. */
    IndexSetup testIndexSetup;

    /** A dictionary for generating random texts. */
    String[] words =
        {
            "bistable", "bistate", "bisyllabic", "bittern", "bitternut", "bitterroot", "bitumen", "blackball",
            "blackballed", "blackballing", "blackballs", "blackbodies", "blackbody", "bladdernut", "bladderwort",
            "blameworthiness", "blameworthy", "blanc", "blatancy", "blather", "blathered", "blatherer", "blathering",
            "blatting", "blazon", "blazoned", "blazoner", "blazoning", "bldg", "bleeps", "blest", "blindside",
            "blindsided", "blindsides", "blindsiding", "blockier", "blocky", "bloodbath", "bloodroot", "blotch",
            "blowtorch", "blueback", "bluebill", "bluebook", "bluebush", "bluegill", "bluegrass", "bluejacket", "boa",
            "boas", "bobble", "bobbled", "bobbles", "bobbling", "bobcat", "bobsled", "bobsledding", "bock", "bockwurst",
            "bodhisattva", "bodyweight", "bogey", "bogeyed", "bogeying", "bogeymen", "bogeys", "bogging", "boggy",
            "bogies", "bogy", "boilermaker", "boilermaker's", "boilermakers", "bolivar", "bolo", "bolometer",
            "bolometer's", "bolometers", "bolos", "bona", "bongo", "bonito", "bonjour", "bonzes", "boogie", "bookbind",
            "bookbinder", "bookbinders", "bookbinding", "bookend", "bookends", "bookkeep", "bookplate", "bookplates",
            "boomtown", "boomtowns", "boosterism", "bootblack", "bootblacks", "bop", "bopping", "borosilicate", "boson",
            "bossier", "bossies", "bossiness", "bossy", "botanic", "botfly", "bottommost", "botulin", "bouillon",
            "boutique", "boutiques", "bowie", "boxier", "boxiness", "boxy", "bpi", "brachia", "brachium", "bracken",
            "brad", "braggadocio", "brakeman", "brakemen", "brakemen's", "brandywine", "bratwurst", "breadfruit",
            "breadfruits", "breastfed", "breastfeed", "breastfeeding", "breastplate", "bribery", "brickmason",
            "brickmasons", "brickyard", "brimming", "brimstone", "brinier", "brininess", "briny", "bristlier",
            "bristly", "bristol", "bristols", "broadloom", "bronc", "bronchiolar", "bronchiolitis", "bronco", "broncos",
            "broncs", "broodiness", "broody", "brookside", "brouhaha", "brucellosis", "brushwork", "bubonic",
            "buckaroo", "buckaroos", "bucketful", "bucketful's", "buckeye", "buckhorn", "bufflehead", "bugaboo",
            "bugbears", "bugeyed", "bulblet", "bullfinch", "bullfrog", "bullhead", "bullheaded", "bullheadedly",
            "bullheadedness", "bullhide", "bullseye", "bullshit", "bullwhackers", "bullyboy", "bullyboys", "bulrush",
            "burble", "burbled", "burbler", "burbles", "burbling", "burg", "burgers", "burlap", "burley", "bushmaster",
            "busyness", "butch", "butene", "butterball", "buttercup", "buttermilk", "buttery", "buttonweed", "bypath",
            "byroad", "cabal", "cabals", "cabana", "cabanas", "cabaret", "cabarets", "cabdriver", "cabdrivers",
            "cabinetmaker", "cabinetmaker's", "cabinetmakers", "cabinetry", "cacao", "cacciatore", "cackly",
            "cacophonist", "cacophony", "cadaver", "cadaverous", "cadaverously", "caddies", "caddy", "cadent",
            "cadenza", "cadet", "cadge", "cadged", "cadger", "cadges", "cadging", "cadmium", "cadre", "caffeine",
            "caffeine's", "cagey", "cageyness", "cahoot", "cahoots", "cairn", "cairned", "cairns", "calamitous",
            "calamitously", "calamitousness", "calcareous", "calcareously", "calcareousness", "calcification",
            "calcified", "calcify", "calcite", "calculability", "calculable", "calculableness", "calculi", "calfskin",
            "californium", "caliphate", "calisthenic", "calisthenics", "callable", "callee", "callee's", "calligraph",
            "calligrapher", "calligraphers", "calligraphy", "calliope", "callow", "callowness", "caloric",
            "calorimeter", "calorimeter's", "calorimeters", "calorimetric", "calorimetry", "calumniate", "calumniated",
            "calumniation", "calumny", "calvary", "calve", "calving", "calypso", "cam", "camaraderie", "camber",
            "cambered", "cambering", "camellias", "cameo", "cameos", "cameraman", "cameramen", "campesinos", "campfire",
            "campground", "campgrounds", "campsite", "campsites", "cams", "cancerous", "cancerously", "candidacy",
            "candlelight", "candlelighter", "canine", "canines", "canister", "canisters", "cannabis", "canneries",
            "cannery", "cannibalism", "cannibalism's", "cannibalistic", "cannier", "canniness", "cannonball", "canny",
            "canonic", "canonist", "cant", "cantaloupe", "cantaloupe's", "cantaloupes", "canted", "canteen", "canteens",
            "canter", "cantered", "canticle", "cantilever", "cantilevers", "canting", "canvasback", "capacitate",
            "capacitative", "capitalistic", "capitulate", "capitulated", "capitulates", "capitulation", "caprice",
            "capsize", "capsized", "capsizes", "capsizing", "capstan", "capstans", "capstone", "capsule", "capsuled",
            "capsules", "capsuling", "captaincy", "captious", "captiously", "captiousness", "caramel", "caraway",
            "carbide", "carbine", "carbines", "carbonaceous", "carbonyl", "carborundum", "carboy", "carbuncle",
            "carbuncled", "carcinogen", "carcinogenic", "carcinogens", "carcinoma", "cardiology", "cardiomegaly",
            "cardiovascular", "careen", "careened", "careening", "careerism", "caretaker", "caretakers", "careworn",
            "caricature", "caricatured", "caricatures", "caricaturist", "carload", "carloading", "carloads", "carmine",
            "carnage", "carnal", "carnality", "carnally", "carob", "carouse", "caroused", "carouser", "carousing",
            "carp", "carped", "carpentry", "carper", "carping", "carpingly", "carport", "carps", "carrageen", "carrel",
            "carrels", "carrion", "carte", "cartels", "cartilage", "cartographer", "cartographers", "cartographic",
            "cartoonist", "cartoonists", "cartwheel", "cartwheeler", "cartwheels", "carven", "casbah", "casebook",
            "casebooks", "casein", "casework", "caseworker", "caseworkers", "cashew", "cashews", "cashmere", "casino",
            "casinos", "cassette", "cassettes", "cassock", "cassocked", "castanet", "castanets", "castigate",
            "castigated", "castigates", "castigation", "castor", "cataclysmic", "catalysis", "catalytic",
            "catalytically", "catapult", "catapulted", "catapulting", "catapults", "catastrophically", "catatonia",
            "catbird", "catchier", "catchword", "catchwords", "catchy", "catechism", "catecholamine", "catecholamines",
            "categoric", "catenate", "catenation", "catfish", "catharsis", "cathodic", "catholicism", "catlike",
            "catnip", "cattail", "cattier", "catties", "cattiness", "cattleman", "cattlemen", "catty", "catwalk",
            "catwalk's", "catwalks", "caucus", "caucuses", "caucusing", "cauliflower", "cauliflowers", "caulk",
            "caulker", "caulks", "causate", "causative", "causatively", "cautionary", "cavalcade", "cavalcades",
            "cavalrymen", "caveman", "cavemen", "cavernous", "cavernously", "caviar", "cavil", "cavort", "cavorted",
            "cavorting", "cayenne", "cede", "ceded", "ceder", "cedilla", "ceding", "celebrant", "celebrants",
            "celerity", "celesta", "cello", "cellophane", "cellos", "cellulose", "celluloses", "censorial", "centaur",
            "centaurs", "centenary", "centennial", "centennially", "centigrade", "centigrades", "centrality",
            "centrifugal", "centrifugally", "centrifugals", "centrifugate", "centrifugation", "centrist", "centroid",
            "centroids", "ceramic", "ceramics", "cerate", "cerated", "cerebellum", "cerebrate", "cerebrated",
            "cerebrates", "cerebrating", "cerebration", "cerebrations", "ceremonious", "ceremoniously",
            "ceremoniousness", "cerise", "cerium", "certiorari", "certitude", "certitudes", "cerulean", "cervical",
            "cesium", "cession", "cetera", "ceteras", "chainlike", "chairlady", "chairwoman", "chairwomen", "chaise",
            "chalkline", "chalky", "chambermaid", "chambermaids", "chamfer", "chamfered", "chamfering", "chamfers",
            "chamois", "champ", "champers", "champs", "chanceries", "chancery", "chancier", "chanciness", "chancy",
            "chantey", "chantries", "chantry", "chaparral", "chaperone", "chaperones", "chapping", "charisma",
            "charismatic", "charitably", "charlatans", "charred", "chartist", "chartists", "chartreuse", "chartroom",
            "chartrooms", "charwomen", "chassis", "chastisement", "chastisements", "chastity", "chateaux", "chatted",
            "chattel", "chattels", "chattier", "chattiness", "chatting", "chatty", "chaw", "checkerboard",
            "checkerboards", "checklist", "checklists", "checksummed", "checksumming", "checkup", "checkups",
            "cheekbone", "cheekbones", "cheekier", "cheekiness", "cheeky", "cheerleader", "cheerleaders", "cheesecloth",
            "cheesier", "cheesiness", "cheesy", "cheetah", "chelate", "chelated", "chelating", "chelation",
            "chelations", "chemic", "chemotherapy", "chemotherapy's", "chevalier", "chevron", "chevroned", "chianti",
            "chic", "chicanery", "chicly", "chicness", "chicory", "chiefdom", "chiefdoms", "chigger", "chiggers",
            "chilblain", "chilblains", "childbearing", "childbirth", "childless", "childlessness", "childlike",
            "childlikeness", "childrearing", "chili", "chimera", "chimeric", "chimpanzee", "chimpanzees", "china",
            "chinchilla", "chinless", "chipboard", "chipped", "chipper", "chipping", "chiropractor", "chiropractors",
            "chive", "chives", "chivying", "chlorate", "chloride", "chlorides", "chlorinate", "chlorinated",
            "chlorinates", "chlorination", "chloroform", "chlorophyll", "choirmaster", "chokeberry", "cholesterol",
            "cholinesterase", "chomp", "choosier", "choosy", "choppier", "choppiness", "choppy", "chorale", "chorales",
            "chordal", "chordata", "chordate", "choreograph", "choreographed", "choreographer", "choreographers",
            "choreographic", "choreography", "chorines", "chortle", "chortled", "chortles", "chortling", "chow",
            "chowder", "chowders", "chrissake", "chromate", "chromatic", "chromaticness", "chromatics", "chromatogram",
            "chromatogram's", "chromatograms", "chromatograph", "chromatographic", "chromatography", "chrome",
            "chromed", "chromes", "chromic", "chroming", "chromite", "chromium", "chromosphere", "chronically",
            "chronograph", "chronography", "chrysalis", "chrysanthemum", "chrysanthemum's", "chrysanthemums", "chub",
            "chubs", "chug", "chugging", "chugs", "chummier", "chumminess", "chumming", "chummy", "chunkier", "chunky",
            "churchgoers", "churchgoing", "churchmen", "churchwoman", "churchwomen", "churl", "churls", "chutney",
            "chutneys", "ciao", "cicada", "cicadas", "cilia", "ciliate", "ciliated", "ciliately", "ciliates", "cinch",
            "cinches", "cinema", "cinemas", "cinematic", "cinnabar", "circa", "circlet", "circulant", "circulatory",
            "circumcise", "circumcised", "circumciser", "circumcises", "circumcising", "circumcision", "circumcisions",
            "circumferential", "circumferentially", "circumpolar", "circumscribe", "circumscribed", "circumscribes",
            "circumscribing", "circumscriptions", "circumspection", "circumspections", "circumsphere", "circumvention",
            "circumventions", "citizenry", "citrate", "citrated", "citric", "citron", "citrus", "citruses", "cityscape",
            "cityscapes", "citywide", "civet", "cladding", "clairvoyance", "clammier", "clamminess", "clamming",
            "clammy", "clamshell", "clamshells", "clandestine", "clandestinely", "clandestineness", "clank", "clanked",
            "clanking", "clankingly", "clannish", "clannishly", "clannishness", "clapboard", "clapboards", "clapped",
            "clapping", "claret", "clarets", "clarinet", "clarinets", "clarion", "classicist", "classier", "classiest",
            "classificatory", "classiness", "classless", "classlessness", "classy", "clattery", "claustrophobia",
            "claustrophobic", "clave", "claver", "clavicle", "clavicle's", "clavicles", "clearcut", "clearheaded",
            "clearheadedly", "clearheadedness", "clearinghouse", "cleat", "cleated", "cleating", "cleats", "clemence",
            "clemency", "clement", "clemently", "clements", "clergymen", "cleric", "clerics", "clientele",
            "cliffhanging", "climactic", "climatological", "climatologically", "climatology", "clinician", "clinicians",
            "clinometer", "clinometer's", "clinometers", "clipboard", "clipboards", "cloakroom", "cloakrooms",
            "clockwatcher", "cloddish", "cloddishness", "clodhopper", "clodhopper's", "clodhoppers", "clomp", "clomped",
            "clomping", "clomps", "closeup", "closeups", "clot", "clothbound", "clothesbrush", "clothesline",
            "clotheslines", "clothesman", "clothesmen", "clothier", "clotted", "clotting", "cloture", "clotured",
            "clotures", "cloturing", "cloudburst", "cloudbursts", "cloy", "cloying", "cloyingly", "clubhouse",
            "clubroom", "clubrooms", "clunkiness", "clunky", "clunkyly", "cm", "coachmen", "coachwork", "coadjutor",
            "coagulable", "coalescence", "coalescent", "coastline", "coattail", "coattails", "coauthor", "cobalt",
            "cobble", "cobbled", "cobbles", "cobblestone", "cobblestoned", "cobblestones", "cobbling", "coble", "cobra",
            "cocaine", "cochineal", "cochlea", "cockatoo", "cockcrow", "cockeye", "cockeyed", "cockeyedly",
            "cockeyedness", "cockier", "cockiness", "cockle", "cocklebur", "cockleshell", "cockpit", "cockpits",
            "cockscomb", "cocksure", "cocky", "coda", "coddle", "coddled", "coddler", "coddles", "coddling", "codebook",
            "codebooks", "codebreak", "codetermine", "codetermines", "codfish", "codicil", "coed", "coedited",
            "coediting", "coeditor", "coeditor's", "coeditors", "coedits", "coeds", "coeducation", "coequal",
            "coequally", "coercible", "coexistent", "coextensive", "coextensively", "cofactor", "cofactors",
            "coffeecup", "coffeepot", "cog", "cognac", "cognate", "cognately", "cognates", "cognation", "cognations",
            "cogs", "cohabitational", "coherency", "cohort", "cohort's", "cohorts", "coiffure", "coiffured",
            "coincident", "coincidently", "coital", "coitally", "coitus", "cola", "colander", "colanders", "colatitude",
            "colatitudes", "coleus", "colicky", "coliform", "coliseum", "collagen", "collapsibility", "collapsible",
            "collarbone", "collard", "collectivities", "collegian", "collegians", "collimate", "collimated",
            "collimates", "collimating", "collimation", "collinear", "collinearity", "collocation", "colloidal",
            "colloidally", "colloquia", "colloquial", "colloquialism", "colloquialism's", "colloquialisms",
            "colloquially", "colloquium", "collude", "colluded", "colludes", "colluding", "collusion", "collusions",
            "colonialism", "colonialist", "colonnade", "colonnaded", "colonnades", "coloration", "coloratura",
            "coltish", "coltishly", "coltishness", "columbines", "columnist", "columnists", "coma", "comas", "comatose",
            "combatted", "combinable", "combo", "combos", "combustible", "combustibles", "comeback", "cometary",
            "comeuppance", "commando", "commandos", "commendable", "commendatory", "commensurable", "commercialism",
            "commies", "commingle", "commingled", "commiserate", "commiserated", "commiserates", "commiserating",
            "commiseration", "commiserative", "commissary", "committable", "committal", "committeeman", "committeemen",
            "committeewoman", "committeewomen", "commodious", "commodiously", "commodiousness", "communicable",
            "communicational", "communique", "communiques", "communism", "communistic", "commutable", "commutate",
            "commutated", "commutates", "commutating", "commutation", "commutations", "compaction", "compatriot",
            "compatriots", "compellable", "compendia", "compensable", "competencies", "competency", "complacency",
            "complacent", "complainant", "complainants", "complaisance", "complaisant", "complaisantly",
            "complementarity", "complementation", "compliant", "compliantly", "comport", "comported", "comportment",
            "compositor", "compositors", "compost", "compote", "compressibility", "compressor", "compressors",
            "compulsive", "compulsively", "compulsiveness", "compulsives", "compulsivity", "con", "concave",
            "conceptuality", "concertina", "concertmaster", "concerto", "concertos", "concessionaire",
            "concessionaires", "conch", "conches", "concierge", "concierges", "conciliate", "conciliated",
            "conciliates", "conciliation", "conciliations", "conciliative", "conciliator", "conciliatory", "conclave",
            "conclaves", "concoct", "concocted", "concocter", "concoctive", "concocts", "concordance", "concordant",
            "concordantly", "concourse", "concourses", "concubine", "concubines", "concussion", "concussions",
            "condemnate", "condemnatory", "condensate", "condensates", "condensations", "condensible", "condescension",
            "condiment", "condiments", "condo", "condo's", "condolence", "condolences", "condominium", "condominium's",
            "condominiums", "condoms", "condos", "conduce", "conduced", "conduces", "conducing", "conductance",
            "conductances", "coneflower", "coney", "confabulate", "confabulated", "confabulates", "confabulation",
            "confabulations", "confect", "confectionery", "confects", "conferee", "conferees", "conferrable",
            "confessional", "confessionally", "confessionals", "confirmatory", "confiscatory", "conflagration",
            "conflagrations", "confluent", "confluents", "conformal", "conformance", "conformation", "conformation's",
            "conformational", "conformationally", "conformations", "conformist", "conformists", "confrontational",
            "confute", "confuted", "confuter", "confutes", "confuting", "congeal", "congealed", "congealing",
            "congeals", "congeniality", "congenital", "congenitally", "congest", "congesting", "congestive", "congests",
            "conglomerate", "conglomerated", "conglomerates", "conglomeration", "conglomerations", "conglomerative",
            "congratulatory", "congregationalism", "congregationalist", "congregationalists", "congressmen",
            "congresswoman", "congresswomen", "congruity", "congruous", "congruously", "congruousness", "conic",
            "conical", "conically", "conicalness", "conics", "conifer", "coniferous", "conifers", "conjectural",
            "conjecturally", "conjoin", "conjoining", "conjoins", "conjoint", "conjointly", "conjugal", "conjugally",
            "conjugate", "conjugated", "conjugately", "conjugateness", "conjugates", "conjugating", "conjugation",
            "conjugations", "conjugative", "conjuncture", "conjunctures", "conk", "conked", "conker", "conkers",
            "conking", "conks", "conn", "conned", "conner", "conning", "connivance", "connive", "connived", "conniver",
            "connives", "conniving", "connotation", "connotations", "connotative", "connotatively", "connubial",
            "connubially", "conquistador", "conquistadores", "conquistadors", "consanguine", "consanguineous",
            "consanguineously", "consanguinity", "conscionable", "conscript", "conscripted", "conscripting",
            "conscription", "conscriptions", "conscripts", "consensual", "consensually", "conservator", "conservatory",
            "consistence", "consonance", "consonantal", "conspiratorial", "conspiratorially", "constance",
            "consternate", "consternated", "consternates", "consternating", "constrict", "constricted", "constricting",
            "constriction", "constrictions", "constrictive", "constrictor", "constrictors", "constricts",
            "constructional", "constructionally", "consular", "cont'd", "contaminant", "contaminants", "contentious",
            "contentiously", "contentiousness", "contestant", "contestants", "continence", "continuant", "contort",
            "contorted", "contorting", "contortion", "contortions", "contortive", "contorts", "contraband",
            "contrabass", "contraception", "contraceptive", "contraceptives", "contradictorily", "contraindicate",
            "contraindicated", "contraindicates", "contraindicating", "contraindication", "contraindication's",
            "contraindications", "contraindicative", "contrarily", "contravene", "contravened", "contravener",
            "contravenes", "contravening", "contravention", "contrite", "contritely", "contriteness", "contrition",
            "controversialists", "controvertible", "contumacy", "contumely", "contusion", "contusions", "convalesce",
            "convalescent", "convalescing", "convection", "convections", "conventionality", "conversationalist",
            "convexity", "conveyor", "conveyors", "convivial", "convivially", "convocation", "convocations", "convoke",
            "convoked", "convokes", "convoking", "convolute", "convolutely", "convolution", "convolutions", "convolve",
            "convolved", "convolves", "detente", "detentions", "deter", "detergency", "detergent", "detergents",
            "determinability", "deterred", "deterrence", "deterrent", "deterrently", "deterrents", "deterring",
            "deters", "detersive", "detersives", "detestation", "detestations", "detonable", "detonator", "detonator's",
            "detonators", "detour", "detoured", "detouring", "detours", "detrimental", "detrimentally", "detune",
            "detuned", "detunes", "detuning", "deuce", "deuced", "deucedly", "deuces", "deucing", "deus", "deuterium",
            "deuteriums", "devaluation", "devalue", "devalued", "devalues", "devaluing", "deviance", "deviances",
            "devious", "deviously", "deviousness", "devoice", "devoiced", "devoices", "devoicing", "devolve",
            "devolved", "devolves", "devolving", "devotional", "devotionally", "dewar", "dewars", "dexter", "dextrous",
            "diabase", "diabetic", "diabetics", "diabolic", "diabolical", "diabolically", "diabolicalness",
            "diachronic", "diachronicness", "diacritical", "diacritically", "diacriticals", "diagnometer",
            "diagnometer's", "diagnometers", "diagnostician", "diagnosticians", "diagrammaticality", "dialectal",
            "dialectally", "dialectic", "dialectical", "dialectically", "dialectics", "dialysis", "diamagnetic",
            "diametric", "diaphanous", "diaphanously", "diaphanousness", "diathermy", "diathesis", "diatom", "diatomic",
            "diatoms", "diatonic", "dichloride", "dichotomous", "dichotomously", "dichotomousness", "dick", "dicker",
            "dickered", "dickering", "dickers", "dickey", "dicks", "dicotyledon", "dictatorial", "dictatorially",
            "dictatorialness", "didactic", "didactics", "diddle", "diddled", "diddler", "diddling", "diehard",
            "diehards", "diem", "diesel", "diesels", "dietaries", "dietary", "dietetic", "dietetics",
            "diethylaminoethyl", "diethylstilbestrol", "dietician", "dieticians", "differentiability", "differentiable",
            "differentiator", "difficile", "diffidence", "diffident", "diffidently", "diffract", "diffracted",
            "diffracting", "diffraction", "diffractions", "diffractometer", "diffractometer's", "diffractometers",
            "diffracts", "diffusible", "digitalis", "dignitaries", "dignitary", "digram", "dihedral", "dilapidate",
            "dilapidated", "dilapidates", "dilapidating", "dilapidation", "dilatation", "dilator", "dilatoriness",
            "dilatory", "dilettante", "dilettantes", "dilithium", "dill", "dillinger", "dilogarithm", "dimensionless",
            "dimethyl", "dimethylglyoxime", "ding", "dinghies", "dinghy", "dingo", "dinnertime", "dinnerware",
            "dinosaur", "dinosaurs", "diocesan", "diocese", "diorama", "dioramas", "dioxalate", "diphthong",
            "diphthongs", "dipodic", "dipody", "dipole", "dipole's", "dipoles", "directivity", "directorate",
            "directorship", "directrices", "directrix", "direful", "direfully", "disablement", "disaffected",
            "disaffectedly", "disaffectedness", "disaffection", "disaffiliate", "disaffiliated", "disaffiliates",
            "disaffiliating", "disaffiliation", "disaggregate", "disaggregated", "disaggregating", "disaggregation",
            "disaggregative", "disapprobation", "disarranged", "disarray", "disarrays", "disarticulated", "disassembly",
            "disavow", "disavowal", "disavowals", "disavowed", "disavowing", "disavows", "disbar", "disbars",
            "disbelief", "discipleship", "discomfit", "discomfited", "discomfiting", "discomfits", "discontinuation",
            "discordant", "discordantly", "discorporate", "discorporated", "discourteous", "discourteously",
            "discourteousness", "discrepant", "discrepantly", "discretionary", "discriminable", "discriminant",
            "discursive", "discursively", "discursiveness", "discus", "discuses", "discussant", "discussants",
            "disdainful", "disdainfully", "disdainfulness", "disembodied", "disembowel", "disembowels",
            "disenchantment", "disengagement", "disequilibrium", "disgruntle", "disgruntles", "disgruntling",
            "disgustful", "disgustfully", "disharmony", "dishevel", "dishevels", "dishonesty", "dishwater",
            "disincentives", "disinclination", "disincorporated", "disinherit", "disinheritance", "disinherited",
            "disinheriting", "disinherits", "disintegrate", "disintegrated", "disintegrates", "disintegrating",
            "disintegration", "disintegrations", "disintegrative", "disinterest", "disinterred", "disjoin", "diskette",
            "diskettes", "dislodgement", "disloyal", "disloyally", "disloyalty", "dismantle", "dismantled",
            "dismantles", "dismantling", "dismembered", "dismemberment", "disobedient", "disobediently", "disoriented",
            "disparage", "disparaged", "disparagement", "disparager", "disparages", "disparaging", "disparagingly",
            "dispassionate", "dispassionately", "dispassionateness", "dispensary", "dispensate", "dispersal",
            "dispersement", "dispersible", "dispositional", "dispossessed", "dispossession", "disproportion",
            "disproportional", "disproportionate", "disproportionately", "disproportionation", "disputable",
            "disputant", "disquietude", "disquisition", "disrepair", "disreputable", "disreputableness", "disrepute",
            "disrespect", "disrobe", "dissect", "dissected", "dissecting", "dissection", "dissects", "dissemble",
            "dissembled", "dissembler", "dissembling", "dissimulation", "dissociable", "dissonant", "dissonantly",
            "dissuade", "dissuaded", "dissuader", "dissuades", "dissuading", "distaff", "distaffs", "distend",
            "distended", "distension", "distillate", "distillates", "distillations", "distilleries", "distillery",
            "distortable", "distributorship", "disulfide", "disunion", "disunited", "disunity", "disuse", "disused",
            "disvalues", "disyllable", "dither", "dithered", "ditherer", "dithering", "ditties", "ditto", "dittos",
            "ditty", "diurnal", "diva", "divalent", "diversionary", "divertimento", "divestiture", "divination",
            "divisible", "divisional", "divisive", "divisively", "divisiveness", "divorcee", "divorcees", "divvied",
            "divvies", "divvying", "dizzily", "doable", "docile", "docilely", "docket", "docketed", "docketing",
            "dockets", "dockside", "dockyard", "doctrinaire", "doctrinal", "doctrinally", "dodecahedra", "dodecahedral",
            "dodecahedron", "doe", "doff", "doffing", "doffs", "doggone", "doggoned", "doggoning", "doghouse", "dogleg",
            "dogmatic", "dogmatically", "dogmatics", "dogtooth", "dogtrot", "dogwood", "doldrum", "doldrums",
            "dolomite", "dolomites", "dolomitic", "dolt", "doltish", "doltishly", "doltishness", "domesticity",
            "domicile", "domiciled", "dominator", "domineer", "domineering", "domineeringly", "domineeringness",
            "domino", "donned", "donning", "donnish", "donnishly", "donnishness", "donnybrook", "donor", "donors",
            "donuts", "doodle", "doodled", "doodler", "doodles", "doodling", "doomsday", "doorbell", "doorkeep",
            "doorkeeper", "doorkeepers", "doorknob", "doorknobs", "doorman", "doormen", "dopant", "dorm", "dormer",
            "dosage", "dosages", "dosimeter", "dosimeter's", "dosimeters", "dosimetry", "dossier", "dossiers", "dotage",
            "dotard", "doubleheader", "doubleton", "doubloon", "dour", "dourly", "dourness", "dovetail", "dowager",
            "dowagers", "dowdier", "dowdies", "dowdiness", "dowdy", "dowel", "dower", "downbeat", "downgrade",
            "downgraded", "downgrades", "downgrading", "downhill", "downpour", "downside", "downslope", "downspout",
            "downswings", "downtrend", "downtrodden", "downturn", "downturns", "downwind", "dowries", "dowry", "dowse",
            "dowser", "dowses", "dowsing", "draftee", "draftees", "dragger", "dragnet", "dragonfly", "dragonhead",
            "dram", "dramatical", "dramaturgy", "dreadnought", "dreamboat", "dreamless", "dreamlessly", "dreamlessness",
            "dreamlike", "dreamt", "dreg", "dressier", "dressiness", "dressmaking", "dressy", "drib", "dribble",
            "dribbled", "dribbler", "dribbles", "dribbling", "dribs", "dripped", "drippier", "dripping", "drippy",
            "drizzle", "drizzled", "drizzles", "drizzling", "drizzlingly", "drizzly", "droll", "drollness", "dromedary",
            "droopier", "droopy", "drophead", "droplet", "droplets", "dropout", "dropouts", "drosophila", "dross",
            "drowse", "drowsed", "drowses", "drowsily", "drowsing", "drub", "drubbing", "drudge", "drudger", "drudges",
            "drudging", "drudgingly", "drugged", "drugging", "drugless", "drugstore", "drugstores", "druid", "drumhead",
            "dryer", "dryers", "dryness", "drywall", "dualism", "dubbed", "ducat", "duce", "duces", "duckling", "duct",
            "ducted", "ductile", "ducting", "ducts", "ductwork", "dud", "duds", "duet", "duets", "duff", "duffel",
            "duffer", "duffers", "dugout", "dukedom", "dulcet", "dulcetly", "dulcify", "dullard", "dumbfound",
            "dumbfounded", "dumbfounder", "dumbfounds", "dumpier", "dumpiness", "dumpy", "dun", "dung", "dunk",
            "dunker", "duopolist", "duopoly", "dupe", "duped", "duper", "dupes", "duping", "dupion", "duplex",
            "duplexer", "duplicable", "duplicity", "durational", "duress", "dustbin", "dustbins", "dutiable", "dwarves",
            "dwelt", "dyad", "dyadic", "dyads", "dynamical", "dynamism", "dynamo", "dynamos", "dynastic", "dysentery",
            "dyspeptic", "dysprosium", "dystopia", "dystrophy", "e'er", "e's", "eardrum", "eardrums", "earphone",
            "earphones", "earsplitting", "earthier", "earthiness", "earthmen", "earthmover", "earthmoving", "earthy",
            "easel", "eastbound", "easternmost", "easygoing", "easygoingness", "eatable", "eatables", "eave",
            "ebullient", "ebulliently", "ecclesiastic", "echelon", "echelons", "echinoderm", "eclectic", "eclectically",
            "ecliptic", "ecological", "ecologically", "ecologists", "econometric", "econometricians", "econometrics",
            "ecosystem", "ecosystems", "ecstatic", "ecstatics", "ecumenic", "ecumenic's", "ecumenical", "ecumenically",
            "ecumenicist", "ecumenicist's", "ecumenicists", "ecumenics", "ecumenist", "ecumenist's", "ecumenists", "ed",
            "edelweiss", "eden", "edgewise", "edgier", "edginess", "edgy", "edification", "edified", "edifies", "edify",
            "edifying", "editorialist", "editorship", "educe", "educing", "eelgrass", "eerily", "efface", "effaceable",
            "effaced", "effacer", "effaces", "effacing", "effectual", "effectualness", "effectuate", "effectuated",
            "effectuates", "effectuating", "effectuation", "efferent", "efferently", "effete", "effetely", "effeteness",
            "efficacious", "efficaciously", "efficaciousness", "effloresce", "efflorescent", "effluent", "effluents",
            "effluvia", "effluvium", "efflux", "effluxion", "effuse", "effused", "effuses", "effusing", "effusion",
            "effusive", "effusively", "effusiveness", "egalitarian", "egalitarianism", "egghead", "eggheaded",
            "eggheadedness", "eggplant", "eggshell", "egocentric", "egotism", "egotist", "egotistic", "egotistical",
            "egotistically", "egotists", "egregious", "egregiously", "egregiousness", "egress", "egret", "egrets", "eh",
            "eider", "eidetic", "eigenstate", "eigenstates", "eigenvector", "eigenvectors", "eightfold", "einsteinium",
            "ejection", "ejector", "ejectors", "elan", "elastomer", "electorate", "electress", "electrician",
            "electricians", "electro", "electrocardiogram", "electrocardiogram's", "electrocardiograms",
            "electrocardiograph", "electrodynamic", "electrodynamicly", "electrodynamics", "electroencephalogram",
            "electroencephalogram's", "electroencephalograms", "electrolysis", "electromagnet", "electromagnetism",
            "electromagnetisms", "electromagnets", "electromyograph", "electromyographic", "electromyographically",
            "electromyography", "electrophoresis", "electrophorus", "electroshock", "electroshocks", "electrostatic",
            "electrostatics", "electrotherapist", "electrotypers", "electroweak", "elegiac", "elegies", "elegy",
            "elephantine", "elfin", "elision", "elisions", "elite", "eliteness", "elites", "ellipsometer",
            "ellipsometer's", "ellipsometers", "ellipsometry", "ellipticity", "elocution", "elope", "eloped", "eloper",
            "elopes", "eloping", "eluate", "eluates", "elute", "eluted", "eluting", "elution", "elysian", "emaciate",
            "emaciates", "emaciating", "emaciation", "emanate", "emanated", "emanates", "emanation", "emanations",
            "emanative", "emancipate", "emancipated", "emancipates", "emancipating", "emasculate", "emasculated",
            "emasculates", "emasculating", "emasculation", "embalm", "embalmer", "embalmers", "embalming", "embalms",
            "embank", "embanked", "embanking", "embankment", "embankments", "embanks", "embarcadero", "embargo",
            "embargoed", "embargoes", "embargoing", "embattle", "embattled", "embattles", "embattling", "embedder",
            "embezzlement", "embittered", "emblematic", "embolden", "emboldened", "emboldens", "emboss", "embossed",
            "embosser", "embossers", "embosses", "embossing", "embower", "embraceable", "embrittle", "embroil",
            "embroiled", "embroiling", "embroils", "embryonic", "emcee", "emceed", "emend", "emendable", "emender",
            "emeritus", "emirate", "emissaries", "emissary", "emission", "emission's", "emissions", "emissivities",
            "emissivity", "emittance", "emitter", "emitters", "emitting", "emolument", "emoluments", "emotionalism",
            "emotionality", "empath", "empathetically", "empathic", "emphysema", "emphysematous", "empiric",
            "empiricism", "emplace", "employability", "emporium", "emporiums", "emulsification", "emulsified",
            "emulsifier", "emulsifies", "emulsify", "emulsion", "emulsions", "encampment", "encase", "encased",
            "encephalitis", "encephalographic", "enchain", "enchained", "enchantress", "enchiladas", "enclave",
            "enclaves", "encomium", "encomiums", "encore", "encored", "encores", "encoring", "encroach", "encroached",
            "encroacher", "encroaches", "encroaching", "encroachment", "encrust", "encrusted", "encrusting", "encrusts",
            "encumbrance", "encumbrancer", "encumbrances", "encyclical", "endearment", "endearments", "endemicity",
            "endgame", "endnote", "endnote's", "endnotes", "endogamous", "endogamy", "endogenous", "endogenously",
            "endosperm", "endothelial", "endothermic", "endpoint", "endpoints", "energetically", "enervate",
            "enervated", "enervates", "enervating", "enervation", "enervative", "enfeeble", "enfeebled", "enfeebles",
            "enfeebling", "enforceability", "enforceable", "enforcible", "engorge", "engorged", "engorges", "engorging",
            "engulfed", "engulfing", "engulfs", "enigma", "enjoinder", "enlargeable", "enmesh", "enmeshed", "enquiries",
            "enquiry", "enrapture", "enraptured", "enraptures", "enrapturing", "enrichment", "enrollee", "enrollees",
            "ensconced", "enshroud", "enslavement", "entailment", "entanglement", "enthalpy", "enthralled",
            "enthralling", "enthrone", "enthroned", "enthrones", "enthroning", "enthuse", "enthused", "enthuses",
            "enthusing", "enticements", "entitlement", "entitlements", "entomb", "entombed", "entomologist",
            "entomology", "entourage", "entourages", "entrain", "entrained", "entrainer", "entraining", "entrains",
            "entranceway", "entrant", "entrants", "entrap", "entrapment", "entrapments", "entrapped", "entraps",
            "entree", "entrees", "entrenchment", "entrenchments", "entrepreneurial", "entrepreneurship", "entwine",
            "entwined", "entwines", "entwining", "enunciable", "enunciate", "enunciated", "enunciates", "enunciating",
            "envenom", "envenomed", "envenoming", "envenoms", "enviable", "enviableness", "enzymatic", "enzymatically",
            "enzyme", "enzymes", "enzymology", "eohippus", "ephemerides", "ephemeris", "epicure", "epicurean",
            "epicycle", "epicycles", "epicyclic", "epicyclical", "epicyclically", "epidemiological",
            "epidemiologically", "epidemiology", "epidermic", "epidermis", "epigenetic", "epigram", "epigrammatic",
            "epigrams", "epigraph", "epigrapher", "epilepsy", "epileptic", "epileptics", "epilogue", "epilogues",
            "epiphany", "epiphenomena", "episcopate", "epistolatory", "epitaxy", "epithelial", "epithelium", "epitome",
            "epitomes", "epochal", "epochally", "epoxy", "equable", "equableness", "equanimities", "equanimity",
            "equestrian", "equestrians", "equidistant", "equidistantly", "equilateral", "equilaterals", "equilibrate",
            "equilibrated", "equilibrates", "equilibrating", "equilibration", "equilibria", "equine", "equines",
            "equinox", "equipotent", "equiproportional", "equiproportionality", "equiproportionate", "equivocal",
            "equivocally", "equivocalness", "equivocation", "eradicable", "erbium", "ergodic", "ergodicity", "erode",
            "eroded", "erodes", "erodible", "eroding", "erosible", "erosion", "erosive", "erosiveness", "erotic",
            "erotica", "erotically", "errancies", "errancy", "errant", "errantly", "errantry", "errants", "errata",
            "erratas", "erratically", "erratum", "ersatz", "erstwhile", "erudite", "eruditely", "erudition", "erupt",
            "erupted", "erupting", "eruptive", "eruptively", "erupts", "escadrille", "escapist", "escarpment",
            "escarpment's", "escarpments", "escritoire", "escrow", "escutcheon", "escutcheons", "esophagi", "esplanade",
            "espousal", "espousals", "essayists", "esters", "estimable", "estimableness", "estimator", "estimators",
            "estoppal", "estrange", "estranged", "estrangement", "estranger", "estranges", "estranging", "estuaries",
            "estuarine", "estuary", "et", "eta", "etas", "etcetera", "etceteras", "etched", "ethane", "ethanol",
            "ethicist", "ethicists", "ethnically", "ethnicities", "ethnicity", "ethnographers", "ethnographic",
            "ethnography", "ethnology", "ethnomethodology", "ethology", "ethos", "ethyl", "ethylene", "etymological",
            "etymologically", "etymologies", "etymology", "eucalyptus", "eugenic", "eugenics", "eulogies", "eulogy",
            "euphemist", "euphony", "euphoric", "eureka", "europium", "euthanasia", "evanescent", "evangelic",
            "evangelical", "evangelicalism", "evangelically", "evangelism", "evangelist", "evangelistic", "evangelists",
            "evasion", "evasions", "evasive", "evasively", "evasiveness", "evensong", "eventide", "eventides",
            "eventuate", "eventuated", "eventuates", "eventuating", "everyman", "evidential", "evidentially",
            "evildoer", "evildoers", "evocable", "evocate", "evocation", "evocations", "evocative", "evocatively",
            "evocativeness", "evolutionists", "ex", "exaltation", "exaltations", "examinable", "excelsior", "excisable",
            "excitability", "excitatory", "exclamatory", "exclusionary", "excoriate", "excoriated", "excoriates",
            "excoriating", "excoriation", "excoriations", "excrescence", "excrescences", "excretory", "excruciate",
            "excruciated", "excruciates", "excruciating", "excruciation", "exculpatory", "excursus", "excursuses",
            "exec", "execrable", "execrableness", "execrate", "execrated", "execrates", "execrating", "execration",
            "execrative", "executrix", "executrixes", "exegesis", "exegete", "exemption", "exemptions", "exercisable",
            "exhilarate", "exhilarated", "exhilarates", "exhilarating", "exhilaratingly", "exhilaration",
            "exhilarative", "exhort", "exhorted", "exhorter", "exhorting", "exhorts", "exhumation", "exhumations",
            "exhume", "exhumed", "exhumer", "exhumes", "exhuming", "exigent", "exigently", "exodus", "exogamous",
            "exogamy", "exogenous", "exogenously", "exonerate", "exonerated", "exonerates", "exonerating",
            "exoneration", "exonerative", "exorciser", "exorcism", "exorcist", "exoskeleton", "exothermic", "exotica",
            "expansible", "expansionist", "expectable", "expectorant", "expectorate", "expectoration", "expediency",
            "expellable", "experiential", "experientially", "experimentalism", "experimentalist", "experimentalist's",
            "experimentalists", "expiable", "expiate", "expiated", "expiates", "expiating", "expiation", "expletive",
            "expletives", "explicable", "explicate", "explicated", "explicates", "explicating", "explication",
            "explicative", "explicatively", "exportation", "exposit", "exposited", "expressionism", "expressionist",
            "expressionistic", "expressionists", "expressionless", "expressionlessly", "expressionlessness",
            "expressway", "expressways", "expurgate", "expurgated", "expurgates", "expurgating", "expurgation",
            "extemporaneous", "extemporaneously", "extemporaneousness", "extempore", "extendibility", "extensional",
            "extensionally", "extensor", "exterminator", "exterminator's", "exterminators", "extern", "externalities",
            "extirpate", "extirpated", "extirpating", "extirpation", "extirpative", "extolled", "extoller", "extolling",
            "extort", "extorted", "extorter", "extorting", "extortive", "extorts", "extracellular", "extracellularly",
            "extraditable", "extralegal", "extralegally", "extramarital", "extraterrestrial", "extravaganza",
            "extravaganzas", "extrema", "extremism", "extricable", "extricate", "extricated", "extricates",
            "extricating", "extrication", "extroversion", "extrovert", "extroverted", "extroverts", "extrude",
            "extruded", "extruder", "extrudes", "extruding", "extrusion", "extrusive", "exuberant", "exuberantly",
            "exudation", "exude", "exuded", "exudes", "exuding", "exultant", "exultantly", "eyeful", "eyelash",
            "eyelashes", "eyeless", "eyelet", "eyelets", "eyesore", "eyesore's", "eyesores", "eyeteeth", "f's",
            "faceplate", "facetious", "facetiously", "facetiousness", "facilitators", "facilitatory", "factious",
            "factiously", "factiousness", "facto", "factuality", "fad", "fadeout", "fads", "faerie", "faery",
            "failsafe", "fairgoer", "fairgoers", "fairgrounds", "fairless", "fairway", "fairways", "falafel",
            "falconry", "falloff", "fallout", "fallouts", "fallow", "fallowness", "familial", "fanatical",
            "fanaticalness", "fanaticism", "fanfare", "fanfold", "fangled", "fanout", "fantasia", "fantasist",
            "fantastically", "farcical", "farcically", "farfetched", "farfetchedness", "farina", "farmland",
            "farmlands", "farmworker", "farmworkers", "farsighted", "farsightedly", "farsightedness", "fascicle",
            "fascicled", "fascicles", "fasciculate", "fasciculated", "fasciculation", "fasciculations", "fascism",
            "fascist", "fascists", "fastidious", "fastidiously", "fastidiousness", "fatalistic", "fatalists", "fateful",
            "fatefully", "fatefulness", "fatherhood", "fatherless", "fatso", "fattier", "fatties", "fattiness", "fatty",
            "fatuity", "fatuous", "fatuously", "fatuousness", "faucet", "faucets", "faun", "fauna", "fax", "fax's",
            "faxes", "faze", "fazed", "fazes", "fazing", "fealty", "fearsome", "fearsomely", "fearsomeness", "feasibly",
            "featherbed", "featherbedding", "featherbrain", "featherbrained", "feathertop", "featherweight", "feathery",
            "febrile", "feces", "fecund", "fecundability", "fecundity", "federalism", "federalist", "federalists",
            "federate", "federated", "federates", "federating", "federations", "federative", "federatively", "fedora",
            "feint", "feinted", "feinting", "feints", "feldspar", "felicitous", "felicitously", "felicitousness",
            "feline", "felinely", "felines", "fella", "fellas", "felon", "felonious", "feloniously", "feloniousness",
            "felons", "felony", "feminism", "femme", "femmes", "fencepost", "fend", "fender", "fenders", "fennel",
            "fermion", "fermion's", "fermions", "fermium", "fernery", "ferret", "ferreted", "ferreter", "ferreting",
            "ferrets", "ferric", "ferris", "ferro", "ferroelectric", "ferromagnet", "ferromagnetic", "ferrous",
            "fervid", "fervidly", "fervidness", "fest", "fester", "festered", "festering", "festers", "fetal", "fete",
            "feted", "fetes", "fetish", "fetishes", "fettle", "fettled", "fettles", "fettling", "feudalistic",
            "feudatory", "fiance", "fiancee", "fiasco", "fiat", "fiats", "fib", "fibbing", "fibrin", "fibrosis",
            "fiche", "fictive", "fictively", "fiddlestick", "fiddlesticks", "fide", "fidget", "fidgeted", "fidgeting",
            "fidgets", "fiducial", "fiducially", "fiduciary", "fief", "fiefdom", "fieldstone", "fieldwork",
            "fieldworker", "fieldworkers", "fiendish", "fiendishly", "fiendishness", "fierily", "fiesta", "fifths",
            "figment", "figural", "figurine", "figurines", "filamentary", "filbert", "filberts", "filch", "filched",
            "filches", "filet", "filets", "filibuster", "filibustered", "filibusterer", "filibustering", "filibusters",
            "filigree", "filigreed", "fillet", "filleted", "filleting", "fillets", "fillies", "filly", "filmdom",
            "filmier", "filminess", "filmstrip", "filmstrips", "filmy", "filtrate", "filtrated", "filtrates",
            "filtrating", "finale", "finale's", "finales", "finalist", "finalists", "finch", "findable", "finesse",
            "finessed", "finessing", "fingernail", "fingernails", "fingerprint", "fingerprinted", "fingerprinting",
            "fingerprints", "fingertip", "fingertips", "finial", "finickiness", "finicky", "fink", "finned", "finny",
            "fireball", "fireballs", "fireboat", "firebreak", "firebreaks", "firebug", "firecracker", "firecrackers",
            "firefight", "firefighters", "firefighting", "firefights", "firehouse", "firehouses", "firemen",
            "firepower", "fireproof", "firewall", "firework", "fishier", "fishmeal", "fishmonger", "fishmongers",
            "fishpond", "fishy", "fissile", "fission", "fissioned", "fissioning", "fissions", "fisticuff", "fisticuffs",
            "fittest", "fivefold", "fizz", "fizzer", "fizzle", "fizzled", "fizzles", "fizzling", "fjord", "fjords",
            "flabbergast", "flabbergasted", "flabbergasting", "flabbergastingly", "flabbergasts", "flagellate",
            "flagellated", "flagellates", "flagellating", "flagellation", "flagman", "flagpole", "flagpoles",
            "flagstaff", "flagstone", "flail", "flailed", "flailing", "flails", "flair", "flak", "flakier", "flakiness",
            "flaky", "flam", "flamboyant", "flamboyantly", "flamen", "flamethrower", "flange", "flanged", "flanges",
            "flapped", "flapper", "flappers", "flashback", "flashbacks", "flashbulb", "flashbulbs", "flashier",
            "flashiness", "flashy", "flatbed", "flathead", "flatiron", "flatirons", "flatland", "flatlander",
            "flatlands", "flatulence", "flatulent", "flatulently", "flatworm", "flautist", "flaxseed", "fleawort",
            "fleck", "flecked", "flecker", "flecking", "flecks", "fledge", "fledges", "fledging", "fletch", "fletched",
            "fletcher", "fletches", "fletching", "fletching's", "fletchings", "flex", "flexed", "flexing", "flexural",
            "flexure", "flimsier", "flimsies", "flimsiness", "flimsy", "flintier", "flintiness", "flintless",
            "flintlock", "flinty", "flipflop", "flippant", "flippantly", "flipped", "flippers", "flipping",
            "flirtation", "flirtations", "flirtatious", "flirtatiously", "flirtatiousness", "flitting", "flocculate",
            "flocculated", "flocculates", "flocculating", "flocculation", "floe", "floes", "flog", "flogged",
            "flogging", "flogs", "floodgate", "floodlight", "floorboard", "floorboards", "flophouses", "flopped",
            "flopping", "floral", "florally", "florid", "floridly", "floridness", "florist", "florists", "flotation",
            "flotations", "flotilla", "flotillas", "flounce", "flounced", "flounces", "flouncing", "floury", "flout",
            "flouted", "flouter", "flouting", "flouts", "flowerpot", "flowstone", "flu", "flub", "flubbed", "flubbing",
            "flubs", "flue", "fluency", "fluff", "fluffs", "fluke", "fluoresce", "fluorescent", "fluorescer",
            "fluoresces", "fluoridate", "fluoridated", "fluoridates", "fluoridating", "fluoridation", "fluoridations",
            "fluoride", "fluorides", "fluorimetric", "fluorinated", "fluorine", "fluorite", "fluorocarbon", "flushable",
            "fluster", "flustered", "flustering", "flusters", "flutist", "flux", "fluxed", "fluxes", "flyaway",
            "flycatcher", "flycatchers", "flywheel", "flywheels", "foal", "foals", "foamier", "foaminess", "foamy",
            "fob", "foible", "foibles", "foist", "foisted", "foisting", "foists", "foldout", "foldouts", "foliate",
            "foliated", "foliates", "foliating", "foliation", "foliations", "folio", "folios", "folklike", "folksier",
            "folksiness", "folksong", "folksongs", "folksy", "follicle", "follicles", "follicular", "followup",
            "followup's", "followups", "foment", "fomented", "fomenter", "fomenting", "foments", "foolhardiness",
            "foolhardy", "footage", "footages", "footbridge", "footbridges", "footfall", "footfalls", "foothill",
            "foothills", "footloose", "footmen", "footpad", "footpads", "footpath", "footstool", "footstools",
            "footwear", "footwork", "fop", "foppery", "foppish", "foppishly", "foppishness", "fops", "forbore",
            "forebears", "foreclosed", "foreclosing", "forefeet", "forefront", "foreknowledge", "foreknown", "foreleg",
            "foremen", "forensic", "forensics", "forepart", "forepaws", "forerunner", "forerunners", "foresaw",
            "foreseeing", "foreshortened", "foreshortening", "forestry", "foreword", "forfeiture", "forfeitures",
            "forfend", "forfended", "forfending", "forfends", "forgo", "forgoer", "forgoing", "forklift", "formability",
            "formaldehyde", "formate", "formates", "formic", "formidably", "formulaic", "forsook", "forswear",
            "forswears", "forthcome", "forthright", "forthrightly", "forthrightness", "fortiori", "fossiliferous",
            "foulmouth", "foulmouthed", "foundling", "foundlings", "fountainhead", "fourfold", "foursome", "foursomes",
            "foursquare", "fourths", "fovea", "foxglove", "foxhole", "foxholes", "foxhound", "foxier", "foxiness",
            "foxtail", "foxy", "foyer", "fracases", "fractionated", "fractionation", "fractious", "fractiously",
            "fractiousness", "fragmentarily", "fragmentation", "francium", "frankfurter", "frankfurters", "franklin",
            "fraudulent", "fraudulentness", "frazzle", "frazzled", "frazzles", "frazzling", "freakish", "freakishly",
            "freakishness", "freeboot", "freebooter", "freebooters", "freeborn", "freedman", "freedmen", "freehand",
            "freehanded"
        };

    /**
     * Builds the tests to be run on a supplied transactional index implementation. This allows the tests in this class
     * to be applied to arbitrary index implementations in sub-classes of this test class.
     *
     * @param testName The name of the unit test.
     * @param testIndex The {@link TransactionalIndex} to test.
     */
    public TransactionalIndexPerfTestBase(String testName, TransactionalIndex testIndex, IndexSetup setup)
    {
        super(testName);

        // Keep reference to the index implementation to test.
        this.testIndex = testIndex;
        this.testIndexSetup = setup;

        // Create a transaction to work on the index in.
        IndexTxId txId1 = IndexTxManager.createTxId();
        t(txId1);

        // Clear the index and load it with the test record set.
        //testIndex.clear();

        // Add a mapping to the index for the sample record.
        IndexMapping mapping = new IndexMapping(new String[] { "text", "title" }, "rating");
        testIndexSetup.addMapping(TestRecord.class, TestRecord.TestRecordSummary.class, mapping);

        // Create some sample records to insert into the index.
        for (int i = 0; i < INDEX_SIZE; i++)
        {
            TestRecord testRecord = new TestRecord(i, randomText(), "TestRecord" + i, 1.0f);
            testIndex.add(testRecord.getKey(), testRecord, testRecord.getSummaryRecord());
            records[i] = testRecord;
        }

        // Commit the ready to test index.
        testIndex.commit();
    }

    public TransactionalIndexPerfTestBase(String testName)
    {
        super(testName);
    }

    public static void main(String[] args)
    {
        // Run a test in read committed mode.
        ProtoIndex readCommittedIndex = new ProtoIndex();
        readCommittedIndex.setTransactionalMode(TransactionalIndex.IsolationLevel.ReadCommitted);

        TransactionalIndexPerfTestBase test =
            new TransactionalIndexPerfTestBase("testIndexPerformance", readCommittedIndex, readCommittedIndex);

        try
        {
            test.testIndexPerformance(1);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Test typical index usage under varying loads.
     *
     * @param n The number of times to read or write to the index per test.
     */
    public void testIndexPerformance(int n) throws Exception
    {
        //log.debug("public void testIndexPerformance(int n) throws Exception: called");
        //log.debug("n = " + n);

        String errorMessage = "";

        for (int i = 0; i < n; i++)
        {
            // Create a local transaction id for this test.
            final IndexTxId txId = IndexTxManager.createTxId();
            t(txId);

            // Used to indicate that a test has completed ok, otherwise it should be rolled back.
            boolean committedOk = false;

            // Randomly select whether this test is to read or update the index based on the ration of read to write
            // transactions mimicking real access.
            if (random.nextFloat() < WRITE_TRANSACTION_RATIO)
            {
                // Do a record update on the index.
                try
                {
                    // Randomly select a record to update the rating of.
                    int key = random.nextInt(INDEX_SIZE);

                    // Generate a new random rating.
                    float newRating = random.nextFloat();

                    // Update the records rating.
                    TestRecord record = records[key];
                    TestRecord.TestRecordSummary newSummaryRecord = record.getSummaryRecord();
                    newSummaryRecord.rating = newRating;

                    // Make an alteration to the record.
                    testIndex.update(record.getKey(), newSummaryRecord);

                    // Commit this transaction.
                    testIndex.commit();
                    committedOk = true;
                }
                finally
                {
                    if (!committedOk)
                    {
                        testIndex.rollback();
                    }
                }
            }
            else
            {
                // Run a search on the index.
                try
                {
                    // Read record1.
                    testIndex.search(randomWord());

                    // Commit this transaction.
                    testIndex.commit();
                    committedOk = true;
                }
                finally
                {
                    if (!committedOk)
                    {
                        testIndex.rollback();
                    }
                }
            }
        }

        // Check that there were no error messages and print them if there were.
        assertTrue("Isolation level is: " + testIndex.getTransationalMode() + "\n" + errorMessage,
            "".equals(errorMessage));
    }

    /**
     * Test index reliability under heavy loading.
     *
     * @param n The number of concurrent transactions to run on the index.
     */
    public void testIndexUnderLoad(int n) throws Exception
    {
        String errorMessage = "";

        // Create a test thread coordinator to run all the test threads under.
        ThreadTestCoordinator tt = new ThreadTestCoordinator(n);

        // Create n transaction threads with equal probability of being a read, rating update or full update.
        for (int i = 0; i < n; i++)
        {
            // Create a transaction id to run the test thread under.
            final IndexTxId txId = IndexTxManager.createTxId();

            // Holds the new test transaction thread.
            TestRunnable nextThread;

            // Randomly select the type of transaction to create.
            float selector = random.nextFloat();

            if (selector < 0.3333333f)
            {
                // Create a read transaction.
                nextThread =
                    new TestRunnable()
                    {
                        public void runWithExceptions() throws Exception
                        {
                            NDC.push(getName());

                            // Set this thread to be associated with its transaction id.
                            // This remains for the life of the thread.
                            t(txId);

                            // Used to determine whether the transaction has been succesfully committed or failed
                            // in the middle and needs to be rolled back.
                            boolean committedOk = false;

                            // Run a search on the index.
                            try
                            {
                                // Read record1.
                                testIndex.search(randomWord());

                                // Commit this transaction.
                                testIndex.commit();
                                committedOk = true;
                            }
                            finally
                            {
                                if (!committedOk)
                                {
                                    testIndex.rollback();
                                }

                                NDC.pop();
                            }
                        }
                    };
            }
            else if ((selector >= 0.3333333f) && (selector < 0.6666666f))
            {
                // Create a ratings update transaction.
                nextThread =
                    new TestRunnable()
                    {
                        public void runWithExceptions() throws Exception
                        {
                            NDC.push(getName());

                            // Set this thread to be associated with its transaction id.
                            // This remains for the life of the thread.
                            t(txId);

                            // Used to determine whether the transaction has been succesfully committed or failed
                            // in the middle and needs to be rolled back.
                            boolean committedOk = false;

                            try
                            {
                                // Randomly select a record to update the rating of.
                                int key = random.nextInt(INDEX_SIZE);

                                // Generate a new random rating.
                                float newRating = random.nextFloat();

                                // Update the records rating.
                                TestRecord record = records[key];
                                TestRecord.TestRecordSummary newSummaryRecord = record.getSummaryRecord();
                                newSummaryRecord.rating = newRating;

                                // Make an alteration to the record.
                                testIndex.update(record.getKey(), newSummaryRecord);

                                // Commit this transaction.
                                testIndex.commit();
                                committedOk = true;
                            }
                            finally
                            {
                                if (!committedOk)
                                {
                                    testIndex.rollback();
                                }
                            }
                        }
                    };
            }
            else
            {
                // Create a complete record update transaction.
                nextThread =
                    new TestRunnable()
                    {
                        public void runWithExceptions() throws Exception
                        {
                            NDC.push(getName());

                            // Set this thread to be associated with its transaction id.
                            // This remains for the life of the thread.
                            t(txId);

                            // Used to determine whether the transaction has been succesfully committed or failed
                            // in the middle and needs to be rolled back.
                            boolean committedOk = false;

                            try
                            {
                                // Randomly select a record to update the rating of.
                                int key = random.nextInt(INDEX_SIZE);

                                // Generate a new random rating.
                                float newRating = random.nextFloat();

                                // Generate a new replacement record for the key.
                                TestRecord newRecord = new TestRecord(key, randomText(), "TestRecord" + key, newRating);

                                // Store the new record locally and update the index.
                                records[key] = newRecord;
                                testIndex.update((long) key, newRecord, newRecord.getSummaryRecord());

                                // Commit this transaction.
                                testIndex.commit();
                                committedOk = true;
                            }
                            finally
                            {
                                if (!committedOk)
                                {
                                    testIndex.rollback();
                                }
                            }
                        }
                    };
            }

            tt.addTestThread(nextThread, i);
        }

        // Run all the threads at once.
        if (n > 0)
        {
            tt.setDeadlockTimeout(DEADLOCK_TIMEOUT);
            tt.run();
            errorMessage += tt.joinAndRetrieveMessages();

            for (Exception e : tt.getExceptions())
            {
                errorMessage += e.getMessage();
                //log.warn("There was an exception: ", e);
            }
        }

        // Check that there were no errors during the test and display them if there were.
        assertTrue("Isolation level is: " + testIndex.getTransationalMode() + "\n" + errorMessage,
            "".equals(errorMessage));
    }

    /**
     * @throws Exception Any exceptions fall through this method and fail the test.
     */
    protected void setUp() throws Exception
    {
        NDC.push(getName());
    }

    /**
     * @throws Exception Any exceptions fall through this method and fail the test.
     */
    protected void tearDown() throws Exception
    {
        NDC.pop();
    }

    /**
     * Helper method that associates specified transaction id with the current thread.
     *
     * @param txId The transaction id.
     */
    private void t(IndexTxId txId)
    {
        IndexTxManager.assignTxIdToThread(txId);
    }

    private String randomText()
    {
        String result = "";

        for (int i = 0; i < TEXT_SIZE; i++)
        {
            result += randomWord() + " ";
        }

        return result;
    }

    private String randomWord()
    {
        //return words[random.nextInt(words.length)];
        return "word";
    }
}
