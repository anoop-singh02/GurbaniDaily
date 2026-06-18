package com.anoop.gurbanidaily

data class Shabad(
    val gurmukhi: String,
    val transliteration: String,
    val meaning: String,
    val source: String
)

object GurbaniData {

    val shabads: List<Shabad> = listOf(
        Shabad(
            gurmukhi = "ਜੋ ਤੁਧੁ ਭਾਵੈ ਸਾਈ ਭਲੀ ਕਾਰ ॥",
            transliteration = "Jo tudh bhaavai saa-ee bhalee kaar.",
            meaning = "Whatever pleases You is the only good deed. When you cannot see which decision is right, surrender the outcome to His Will and make the most honest choice you can.",
            source = "Japji Sahib — Guru Nanak Dev Ji, Ang 3"
        ),
        Shabad(
            gurmukhi = "ਚਿੰਤਾ ਤਾ ਕੀ ਕੀਜੀਐ ਜੋ ਅਨਹੋਨੀ ਹੋਇ ॥\nਇਹੁ ਮਾਰਗੁ ਸੰਸਾਰ ਕੋ ਨਾਨਕ ਥਿਰੁ ਨਹੀ ਕੋਇ ॥",
            transliteration = "Chintaa taa kee keejee-ai jo anhonee ho-ay.\nIhu maarag sansaar ko Naanak thir nahee ko-ay.",
            meaning = "Worry only about that which never happens. This is the way of the world — nothing here is permanent. Most of what breaks our heart is fear of things that may never come.",
            source = "Salok — Guru Tegh Bahadur Ji, Ang 1429"
        ),
        Shabad(
            gurmukhi = "ਨਾਨਕ ਨਾਮ ਚੜ੍ਹਦੀ ਕਲਾ ॥ ਤੇਰੇ ਭਾਣੇ ਸਰਬਤ ਦਾ ਭਲਾ ॥",
            transliteration = "Naanak Naam chardee kalaa. Tere bhaane sarbat daa bhalaa.",
            meaning = "Through the Naam, the spirit rises ever higher. In Your Will, may there be wellbeing for all. The steady inner lift that holds even through hard seasons.",
            source = "Ardas"
        ),
        Shabad(
            gurmukhi = "ਦੁਖੁ ਦਾਰੂ ਸੁਖੁ ਰੋਗੁ ਭਇਆ ਜਾ ਸੁਖੁ ਤਾਮਿ ਨ ਹੋਈ ॥",
            transliteration = "Dukh daaroo sukh rog bha-i-aa jaa sukh taam na ho-ee.",
            meaning = "Suffering is the medicine, and comfort the disease — for where there is comfort, there is no remembrance of Him. The hard seasons are often the ones that turn us back toward what matters.",
            source = "Guru Nanak Dev Ji, Ang 469"
        ),
        Shabad(
            gurmukhi = "ਮਨ ਤੂੰ ਜੋਤਿ ਸਰੂਪੁ ਹੈ ਆਪਣਾ ਮੂਲੁ ਪਛਾਣੁ ॥",
            transliteration = "Man toon jot saroop hai aapnaa mool pachhaan.",
            meaning = "O my mind, you are the embodiment of the Divine Light — recognise your own origin. You carry something far greater within you than the worry of this moment.",
            source = "Guru Amar Das Ji, Ang 441"
        ),
        Shabad(
            gurmukhi = "ਹਰਿ ਜਿਉ ਰਾਖੈ ਤਿਉ ਰਹੀਐ ਜੋ ਦੇਵੈ ਸੋਈ ਖਾਈਐ ॥",
            transliteration = "Har ji-o raakhai ti-o rahee-ai jo dayvai so-ee khaa-ee-ai.",
            meaning = "As He keeps us, so we live; what He gives, that we receive. Trust in His arrangement — you are provided for, even when you cannot yet see how.",
            source = "Guru Arjan Dev Ji, Ang 628"
        ),
        Shabad(
            gurmukhi = "ਜਿਨੀ ਨਾਮੁ ਧਿਆਇਆ ਗਏ ਮਸਕਤਿ ਘਾਲਿ ॥\nਨਾਨਕ ਤੇ ਮੁਖ ਉਜਲੇ ਕੇਤੀ ਛੁਟੀ ਨਾਲਿ ॥",
            transliteration = "Jinee Naam dhi-aa-i-aa ga-ay maskat ghaal.\nNaanak te mukh ujle ketee chhutee naal.",
            meaning = "Those who meditate on the Naam and depart after working by the sweat of their brow — their faces are radiant, and many are saved along with them. Honest effort is itself a form of devotion.",
            source = "Japji Sahib — Guru Nanak Dev Ji, Ang 8"
        ),
        Shabad(
            gurmukhi = "ਨਾਨਕ ਚਿੰਤਾ ਮਤਿ ਕਰਹੁ ਚਿੰਤਾ ਤਿਸ ਹੀ ਹੇਇ ॥",
            transliteration = "Naanak chintaa mat karahu chintaa tis hee he-ay.",
            meaning = "O Nanak, do not be anxious — the One who created the world takes care of it. The same Hand that brought you here is still holding you.",
            source = "Guru Nanak Dev Ji, Ang 955"
        ),
        Shabad(
            gurmukhi = "ਜਬ ਆਵੈ ਸੰਤੋਖ ਮਨਿ ਤਬ ਪੂਰੇ ਸਭ ਕਾਮ ॥",
            transliteration = "Jab aavai santokh man tab poore sabh kaam.",
            meaning = "When contentment enters the mind, then all tasks are accomplished. Peace is not on the other side of the decision — it is what lets you make it.",
            source = "Guru Arjan Dev Ji, Ang 1362"
        ),
        Shabad(
            gurmukhi = "ਸੁਖੁ ਦੁਖੁ ਦੋਨੋ ਸਮ ਕਰਿ ਜਾਨੈ ਅਉਰੁ ਮਾਨੁ ਅਪਮਾਨਾ ॥",
            transliteration = "Sukh dukh dono sam kar jaanai a-or maan apmaanaa.",
            meaning = "One who knows pleasure and pain as the same, and honour and dishonour alike — walks steady through every season. The storm does not last; the one who steadies through it does.",
            source = "Guru Tegh Bahadur Ji, Ang 633"
        ),
        Shabad(
            gurmukhi = "ਜੋ ਸਰਣਿ ਆਵੈ ਤਿਸੁ ਕੰਠਿ ਲਾਵੈ ਇਹੁ ਬਿਰਦੁ ਸੁਆਮੀ ਸੰਦਾ ॥",
            transliteration = "Jo saran aavai tis kanth laavai ihu birad su-aamee sandaa.",
            meaning = "Whoever comes to His Sanctuary, He hugs close to His heart — this is the way of the Master. When you feel lost, you are never turned away.",
            source = "Guru Arjan Dev Ji, Ang 544"
        ),
        Shabad(
            gurmukhi = "ਮਨਿ ਜੀਤੈ ਜਗੁ ਜੀਤੁ ॥",
            transliteration = "Man jeetai jag jeet.",
            meaning = "Conquer your own mind, and you conquer the world. Every outer battle you face is first won within.",
            source = "Japji Sahib — Guru Nanak Dev Ji, Ang 6"
        ),
        Shabad(
            gurmukhi = "ਅਕਾਲ ਮੂਰਤਿ ਅਜੂਨੀ ਸੈਭੰ ਗੁਰ ਪ੍ਰਸਾਦਿ ॥",
            transliteration = "Akaal moorat ajoonee saibhan gur prasaad.",
            meaning = "Beyond death, beyond birth, self-existent, known by the Guru's grace. A reminder of what is constant when everything around you feels uncertain.",
            source = "Mool Mantar — Japji Sahib"
        ),
        Shabad(
            gurmukhi = "ਤੂੰ ਮੇਰਾ ਪਿਤਾ ਤੂੰਹੈ ਮੇਰਾ ਮਾਤਾ ॥ ਤੂੰ ਮੇਰਾ ਬੰਧਪੁ ਤੂੰ ਮੇਰਾ ਭ੍ਰਾਤਾ ॥",
            transliteration = "Toon meraa pitaa toonhai meraa maataa. Toon meraa bandhap toon meraa bhraataa.",
            meaning = "You are my Father, You are my Mother. You are my Relative, You are my Brother. When you feel alone in a decision, you are not alone.",
            source = "Guru Arjan Dev Ji, Ang 103"
        ),
        Shabad(
            gurmukhi = "ਜੇ ਸੁਖੁ ਦੇਹਿ ਤ ਤੁਝਹਿ ਅਰਾਧੀ ਦੁਖਿ ਭੀ ਤੁਝੈ ਧਿਆਈ ॥",
            transliteration = "Je sukh deh ta tujheh araadhee dukh bhee tujhai dhi-aa-ee.",
            meaning = "If You give me happiness, I worship You; even in pain, I meditate on You. Let neither joy nor sorrow pull you from your centre.",
            source = "Guru Arjan Dev Ji, Ang 757"
        )
    )
}
