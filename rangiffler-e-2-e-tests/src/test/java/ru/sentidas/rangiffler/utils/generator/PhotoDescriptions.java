package ru.sentidas.rangiffler.utils.generator;
import java.util.*;

public final class PhotoDescriptions {
    private static final Random RND = new Random();


// ---------- Наборы фраз (по ~30 на язык), без эмодзи и без точки в конце ----------

    // RU — Русский
    private static final List<String> RU = List.of(
            "Ооо, какие огромные горы!",
            "Никогда не видел такого чистого неба — дышится свободой",
            "Это место будто остановило время. Хочу запомнить каждый миг",
            "Солнце тонет в море, а я тону в эмоциях",
            "Здесь каждый шаг — приключение, а каждый взгляд — открытка",
            "Ветер шепчет истории странников. Слушаю и улыбаюсь",
            "Лёгкая усталость в ногах и огромное счастье в сердце",
            "Город просыпается, и вместе с ним просыпаюсь я",
            "Горы зовут, и я иду — навстречу себе",
            "Этот закат навсегда со мной, даже когда закрою глаза",
            "Каменные улочки и запах свежего хлеба — как из кино",
            "Море убаюкивает, а мысли становятся тише",
            "Тут можно просто быть. Без «надо»",
            "Мир огромен, а мы — достаточно смелые, чтобы его почувствовать",
            "Утро с видом, от которого сердце бьётся громче",
            "Кофе, тёплый ветер и планы потеряться в городе",
            "Каждый поворот — маленькое чудо",
            "Яркие краски, смешные вывески и бесконечная улыбка",
            "Ночь такая звёздная, что хочется шептать",
            "Когда дорога ведёт в неведомое — это и есть свобода",
            "С глаз долой — заботы, с горизонта — границы",
            "Дышу глубже, чувствую ярче, живу громче",
            "Снова влюбляюсь в мир. И в себя в нём",
            "Тишина здесь звучит лучше любой музыки",
            "Скалы, волны и уверенность, что всё возможно",
            "Городок, где каждый домик — характер, а каждый поворот — история",
            "В этом утре так много надежды",
            "Усталость приятная, как объятие после долгого пути",
            "Маленькая точка на карте, а сколько счастья!",
            "Пусть память хранит этот свет и тепло"
    );

    // EN — Английский
    private static final List<String> EN = List.of(
            "Oh wow, these mountains are massive!",
            "Never seen a sky so clear — I can finally breathe",
            "Time slows down here, and I just listen",
            "Sun melting into the sea, and me into the moment",
            "Every step is a story; every corner is a postcard",
            "The wind carries travelers’ tales. I’m all ears",
            "Tired feet, happy heart — perfect combo",
            "The city wakes up and so do I",
            "The mountains are calling, and I must go",
            "This sunset is a keeper — eyes closed, heart open",
            "Stone streets, fresh bread, and a film-like day",
            "The sea hushes the noise, and I hear myself",
            "Here I can simply be. No «should»",
            "The world is huge, and I’m brave enough to feel it",
            "Morning view making my heartbeat louder",
            "Coffee, warm breeze, and plans to get lost",
            "Tiny miracles at every turn",
            "Bright colors, funny signs, and a grin I can’t hide",
            "A starry night that whispers",
            "Road to the unknown: freedom at its finest",
            "Out of sight: worries. Out of horizon: borders",
            "Breathing deeper, feeling brighter, living louder",
            "Falling in love with the world. And my place in it",
            "Silence that sounds better than music",
            "Cliffs, waves, and the courage to try",
            "A town where every house has a soul",
            "This morning tastes like hope",
            "The sweetest kind of exhaustion",
            "A tiny dot on the map—massive joy",
            "Let this light stay with me"
    );

    // ES — Испанский
    private static final List<String> ES = List.of(
            "¡Wow, qué montañas tan enormes!",
            "Nunca vi un cielo tan claro: por fin respiro",
            "Aquí el tiempo se detiene y yo solo escucho",
            "El sol se hunde en el mar y yo en el momento",
            "Cada paso es una historia; cada esquina, una postal",
            "El viento trae cuentos de viajeros. Los escucho",
            "Pies cansados, corazón feliz",
            "La ciudad despierta y yo con ella",
            "Las montañas me llaman y tengo que ir",
            "Atardecer para guardar en el alma",
            "Calles de piedra y pan recién hecho: de película",
            "El mar apaga el ruido y me oigo por fin",
            "Aquí puedo simplemente ser",
            "El mundo es enorme y quiero sentirlo todo",
            "Una vista que acelera el corazón",
            "Café, brisa cálida y planes de perderme",
            "Pequeños milagros en cada giro",
            "Colores vivos, letreros graciosos y mi sonrisa",
            "Una noche estrellada que susurra",
            "Carretera a lo desconocido: pura libertad",
            "Lejos las preocupaciones, lejos las fronteras",
            "Respiro hondo, siento fuerte, vivo alto",
            "Me enamoro del mundo de nuevo",
            "Silencio que suena mejor que la música",
            "Acantilados, olas y valor para intentar",
            "Un pueblo donde cada casa tiene alma",
            "Esta mañana sabe a esperanza",
            "Cansancio dulce que abraza",
            "Un punto en el mapa, una alegría gigante",
            "Que esta luz me acompañe"
    );

    // ZH — Китайский (упрощённый)
    private static final List<String> ZH = List.of(
            "哇，这里的山太壮观了！",
            "从未见过这么清澈的天空，我终于深呼吸",
            "在这里，时间慢了下来，我只需要聆听",
            "夕阳沉入大海，我沉入此刻",
            "每一步都是故事，每个转角都是明信片",
            "风里有旅人的传说，我听见了",
            "脚步疲惫，心却很轻",
            "城市醒来，我也醒来",
            "山在召唤，我必须出发",
            "这个日落值得珍藏",
            "石板路和面包香，像电影一样",
            "海浪抚平了喧嚣，我听见了自己",
            "在这里，我可以只“存在”",
            "世界很大，我想认真去感受",
            "清晨的风景让心跳更响",
            "咖啡、暖风，还有迷路的计划",
            "每个转弯都有小小的惊喜",
            "鲜艳的色彩、可爱的招牌和停不下的笑",
            "星空在耳边轻语",
            "向未知的路上，才是自由",
            "视线之外是烦恼，地平线之外是边界",
            "呼吸更深，感受更真，生活更亮",
            "再次爱上这个世界",
            "安静比音乐更动人",
            "悬崖与海浪，给我勇气",
            "小镇里每一栋房子都有灵魂",
            "这清晨尝起来像希望",
            "甜甜的疲惫把我拥抱",
            "地图上的小点，却装满了快乐",
            "愿这束光一直伴我"
    );

    // HI — Хинди
    private static final List<String> HI = List.of(
            "वाह, कितने विशाल पहाड़!",
            "इतना साफ़ आकाश कभी नहीं देखा — साँस भर आई",
            "यहाँ समय धीमा हो जाता है, मैं बस सुनता हूँ",
            "सूरज समंदर में डूबा, मैं इस पल में",
            "हर क़दम एक कहानी, हर मोड़ एक पोस्टकार्ड",
            "हवा यात्रियों की दास्तान लाती है, मैं सुन रहा हूँ",
            "थकी टांगें, खुश दिल",
            "शहर जागा और मैं भी",
            "पहाड़ बुला रहे हैं, मुझे जाना है",
            "ये सूर्योदय/सूर्यास्त दिल में सहेज लूँगा",
            "पत्थर की गलियाँ और ताज़ा रोटी — फिल्म जैसा",
            "समंदर शोर को थमा देता है, मैं खुद को सुनता हूँ",
            "यहाँ मैं बस ‘हूँ’",
            "दुनिया बड़ी है, और मैं उसे महसूस करना चाहता हूँ",
            "सुबह का नज़ारा दिल की धड़कन तेज कर देता है",
            "कॉफी, गरम हवा और खो जाने की योजना",
            "हर मोड़ पर छोटा सा चमत्कार",
            "चटख रंग, मजेदार साइन और मेरी मुस्कान",
            "तारों भरी रात कानों में फुसफुसाती है",
            "अनजाने रास्ते ही असली आज़ादी हैं",
            "नज़र से दूर चिंताएँ, क्षितिज से दूर सीमाएँ",
            "गहरी साँस, गहरी भावना, खुलकर जीना",
            "दुनिया से फिर मोहब्बत हो गई",
            "ये सन्नाटा संगीत से सुंदर है",
            "चट्टानें, लहरें और हिम्मत",
            "इस कस्बे के हर घर में एक कहानी है",
            "सुबह उम्मीद जैसी लगती है",
            "मीठी थकान बाँहें डाल लेती है",
            "मानचित्र पर छोटा बिंदु, दिल में बड़ा सुख",
            "ये रोशनी साथ चले"
    );

    // AR — Арабский
    private static final List<String> AR = List.of(
            "يا لها من جبال شاهقة!",
            "لم أرَ سماءً بهذا الصفاء من قبل",
            "هنا يتباطأ الوقت وأصغي فقط",
            "تغيب الشمس في البحر وأغيب في اللحظة",
            "كل خطوة حكاية، وكل زاوية بطاقة بريدية",
            "الريح تحمل قصص المسافرين",
            "تعبٌ لذيذ وقلبٌ سعيد",
            "تصحو المدينة وأصحو معها",
            "الجبال تناديني ولا بد أن ألبّي",
            "غروب يستحق أن يُحفظ في القلب",
            "أزقة حجر وخبز طازج — كأنه فيلم",
            "البحر يهدّئ الضجيج فأسمع نفسي",
            "هنا أكون كما أنا",
            "العالم واسع وأريد أن أشعر به حقاً",
            "صباحٌ يسرّع نبض قلبي",
            "قهوة ونسيمٌ دافئ وخطة للتيه",
            "معجزة صغيرة في كل منعطف",
            "ألوان زاهية ولافتات طريفة وابتسامة لا تزول",
            "ليلٌ مُرصّع بالنجوم يهمس",
            "الطريق إلى المجهول هو الحرية",
            "خارج النظر: الهموم. وخارج الأفق: الحدود",
            "أتنفس أعمق وأحس أكثر وأعيش أكبر",
            "أقع في حب العالم من جديد",
            "صمتٌ أجمل من الموسيقى",
            "صخور وأمواج وشجاعة",
            "بلدة لكل بيت فيها روح",
            "صباح بطعم الأمل",
            "إرهاقٌ حلو كالعناق",
            "نقطة صغيرة على الخريطة، وفرح كبير",
            "لترافقني هذه الإضاءة"
    );

    // PT — Португальский
    private static final List<String> PT = List.of(
            "Uau, que montanhas enormes!",
            "Nunca vi um céu tão limpo — respiro fundo",
            "Aqui o tempo desacelera e eu apenas ouço",
            "O sol se deita no mar e eu neste momento",
            "Cada passo é uma história; cada esquina, um postal",
            "O vento traz histórias de viajantes",
            "Pés cansados, coração feliz",
            "A cidade desperta e eu também",
            "As montanhas chamam e eu vou",
            "Pôr do sol para guardar no coração",
            "Ruelas de pedra e pão quentinho — cinema puro",
            "O mar silencia o barulho e eu me escuto",
            "Aqui eu posso simplesmente ser",
            "O mundo é enorme e eu quero senti-lo",
            "A vista da manhã acelera o coração",
            "Café, brisa morna e planos de me perder",
            "Pequenos milagres a cada curva",
            "Cores vivas, placas divertidas e meu sorriso",
            "Noite estrelada que sussurra",
            "Estrada para o desconhecido: liberdade",
            "Fora de vista: preocupações. Fora do horizonte: limites",
            "Respiro mais fundo, sinto mais forte, vivo mais alto",
            "Apaixonando-me pelo mundo de novo",
            "Silêncio mais bonito que música",
            "Falésias, ondas e coragem",
            "Uma vila onde cada casa tem alma",
            "A manhã tem gosto de esperança",
            "Cansaço doce que abraça",
            "Um ponto no mapa, uma alegria gigante",
            "Que esta luz siga comigo"
    );

    // FR — Французский
    private static final List<String> FR = List.of(
            "Oh, ces montagnes immenses !",
            "Jamais vu un ciel aussi clair — ça fait du bien",
            "Ici, le temps ralentit et j’écoute",
            "Le soleil se fond dans la mer, et moi dans l’instant",
            "Chaque pas est une histoire, chaque coin une carte postale",
            "Le vent porte des récits de voyageurs",
            "Pieds fatigués, cœur heureux",
            "La ville s’éveille, moi aussi",
            "Les montagnes m’appellent, je dois y aller",
            "Un coucher de soleil à garder précieusement",
            "Ruelles pavées et pain chaud — comme au cinéma",
            "La mer apaise le bruit, je m’entends à nouveau",
            "Ici je peux simplement être",
            "Le monde est vaste, je veux le ressentir",
            "La vue du matin fait vibrer mon cœur",
            "Café, brise tiède et plan de me perdre",
            "De petits miracles à chaque virage",
            "Des couleurs vives, des enseignes drôles, et mon sourire",
            "Une nuit étoilée qui chuchote",
            "Route vers l’inconnu : la liberté",
            "Hors de vue : les soucis. Hors horizon : les frontières",
            "Je respire plus profond, je sens plus fort, je vis plus haut",
            "Je retombe amoureux du monde",
            "Un silence plus beau que la musique",
            "Falaises, vagues et courage",
            "Un village où chaque maison a une âme",
            "Ce matin a le goût de l’espoir",
            "Une douce fatigue qui enlace",
            "Un point sur la carte, une joie immense",
            "Que cette lumière m’accompagne"
    );

    // BN — Бенгальский
    private static final List<String> BN = List.of(
            "অসাধারণ উঁচু পাহাড়!",
            "এত স্বচ্ছ আকাশ কখনও দেখিনি — নিঃশ্বাস ভরে যাই",
            "এখানে সময় ধীরে চলে, আমি শুধু শুনি",
            "সূর্য সমুদ্রে ডুবে যায়, আমিও মুহূর্তে",
            "প্রতিটি পদক্ষেপ গল্প; প্রতিটি মোড় পোস্টকার্ড",
            "হাওয়ায় ভেসে আসে ভ্রমণকারীর কাহিনি",
            "ক্লান্ত পা, খুশি হৃদয়",
            "শহর জাগে, আমিও জাগি",
            "পাহাড় ডাকছে, যেতেই হবে",
            "এই সূর্যাস্ত মনে রেখে দেব",
            "পাথরের গলি ও গরম রুটি — ছবির মতো",
            "সমুদ্র কোলাহল থামায়, নিজের কথা শুনি",
            "এখানে আমি শুধু ‘আছি’",
            "পৃথিবী বড়, আমি অনুভব করতে চাই",
            "সকালের দৃশ্য হৃদয় কাঁপায়",
            "কফি, উষ্ণ হাওয়া ও হারিয়ে যাওয়ার পরিকল্পনা",
            "প্রতি মোড়ে ছোট্ট বিস্ময়",
            "রঙিন পথ ও মজার সাইনবোর্ড, হাসি লুকোনো যায় না",
            "তারাভরা রাত ফিসফিস করে",
            "অজানার পথে — স্বাধীনতা",
            "চোখের আড়ালে দুশ্চিন্তা, দিগন্তের ওপারে সীমানা",
            "গভীর শ্বাস, গভীর অনুভব, বড় করে বাঁচা",
            "আবার পৃথিবীর প্রেমে পড়লাম",
            "নীরবতা সঙ্গীতের চেয়েও সুন্দর",
            "খাড়া পাথর, ঢেউ আর সাহস",
            "এ গ্রামের প্রতিটি বাড়িরই নিজস্ব প্রাণ আছে",
            "সকালটায় আশার স্বাদ",
            "মিষ্টি ক্লান্তি জড়িয়ে ধরে",
            "মানচিত্রে ছোট্ট বিন্দু, আনন্দে ভরা",
            "এই আলোটি সাথে থাকুক"
    );

    // DE — Немецкий
    private static final List<String> DE = List.of(
            "Wow, was für gewaltige Berge!",
            "So einen klaren Himmel habe ich selten gesehen",
            "Hier wird die Zeit langsam, und ich höre zu",
            "Die Sonne sinkt ins Meer, ich in den Moment",
            "Jeder Schritt ist eine Geschichte, jede Ecke eine Postkarte",
            "Der Wind bringt Geschichten von Reisenden",
            "Müde Füße, glückliches Herz",
            "Die Stadt erwacht – ich auch",
            "Die Berge rufen, ich muss los",
            "Diesen Sonnenuntergang behalte ich im Herzen",
            "Steingassen und frisches Brot — wie im Film",
            "Das Meer dämpft den Lärm, ich höre mich selbst",
            "Hier darf ich einfach sein",
            "Die Welt ist groß, ich will sie fühlen",
            "Der Morgenblick lässt mein Herz schneller schlagen",
            "Kaffee, warmer Wind und der Plan, mich zu verlaufen",
            "Kleine Wunder an jeder Kurve",
            "Leuchtende Farben, witzige Schilder und mein Grinsen",
            "Ein Sternenhimmel, der flüstert",
            "Straße ins Unbekannte: Freiheit pur",
            "Außer Sicht: Sorgen. Außer Horizont: Grenzen",
            "Tiefer atmen, stärker fühlen, lauter leben",
            "Neu verliebt in die Welt",
            "Stille, schöner als Musik",
            "Klippen, Wellen und Mut",
            "Ein Ort, an dem jedes Haus eine Seele hat",
            "Dieser Morgen schmeckt nach Hoffnung",
            "Süße Müdigkeit, die umarmt",
            "Kleiner Punkt auf der Karte, große Freude",
            "Möge dieses Licht mich begleiten"
    );

    // Регистрация наборов
// --- фрагмент PhotoDescriptions.java ---

    // Регистрация наборов фраз по языкам.
// Добавлены также faker-локали (zh-CN, pt-BR, pt-PT) к тем же спискам,
// чтобы можно было передавать тег "как есть" из languageTagByCountry(..).
    private static final Map<String, List<String>> DESCRIPTIONS = Map.ofEntries(
            Map.entry("ru", RU),  // Россия

            // Английский — США, Великобритания, Австралия, Новая Зеландия, Канада, Ирландия
            Map.entry("en", EN),  // США, Великобритания, Австралия, Новая Зеландия, Канада, Ирландия

            // Испанский — Испания, Мексика, Аргентина, Колумбия
            Map.entry("es", ES),  // Испания, Мексика, Аргентина, Колумбия

            // Китайский (упрощённый) — Китай + faker-локаль zh-CN
            Map.entry("zh", ZH),     // Китай (упрощённый китайский)
            Map.entry("zh-CN", ZH),  // faker-локаль: zh-CN → используем те же фразы

            // Хинди — Индия
            Map.entry("hi", HI),  // Индия (хинди)

            // Арабский — Саудовская Аравия, ОАЭ, Египет, Марокко
            Map.entry("ar", AR),  // Саудовская Аравия, ОАЭ, Египет, Марокко

            // Португальский — Португалия, Бразилия + faker-локали pt-BR, pt-PT
            Map.entry("pt", PT),     // Португалия, Бразилия
            Map.entry("pt-BR", PT),  // faker-локаль: pt-BR → те же фразы
            Map.entry("pt-PT", PT),  // faker-локаль: pt-PT → те же фразы

            Map.entry("fr", FR),  // Франция
            Map.entry("bn", BN),  // Бангладеш (бенгальский)
            Map.entry("de", DE)   // Германия
    );

    /**
     * Берёт фразу по тегу, как его вернул ваш UserDataGenerator.languageTagByCountry
     * (например: "ru", "en", "zh-CN", "pt-BR"...).
     */
    public static String randomByTag(String localeTag) {
        // 1) Сразу пробуем точное совпадение ключа (учитывая региональные теги, типа "pt-BR").
        List<String> bag = DESCRIPTIONS.get(localeTag);

        // 2) Если не нашли (или пришёл в другом регистре, например "ZH-CN"),
        //    пробуем ещё раз — с привидением к нижнему регистру.
        if (bag == null) {
            bag = DESCRIPTIONS.get(localeTag == null ? null : localeTag.toLowerCase(Locale.ROOT));
        }

        // 3) Если ключ по-прежнему не найден (null/неподдержанный тег),
        //    используем английский набор как безопасный fallback.
        if (bag == null) bag = DESCRIPTIONS.get("en");

        // 4) Возвращаем случайную фразу из выбранного набора.
        return bag.get(RND.nextInt(bag.size()));
    }
}
