load("script/utils.js")
load("script/ScriptData.js")
load("script/questinfo.js")

Optional = Java.type("java.util.Optional")

function header() {
    return ["進捗詳細"]
}

function begin() {}

function body(quest) {
    return toComparable([getProgress(quest.no, quest.type, quest.progressFlag)])
}

function end() {}

function getProgress(questNo, questType, questProgressFlag) {
    if (questType !== QUEST_TYPE.ONCE) {
        if (questNo in QUEST_DATA) {
            var sum = 0
            var result = ""
            var conditions = QUEST_DATA[questNo]
            conditions.forEach(function (condition, i) {
                var count = getQuestCount(questNo, i + 1)
                var max = condition.max
                var rate = Math.min(count, max) / max * 100
                sum += rate
                result += Optional.ofNullable(condition.title).map(function (title) {
                    return " " + title + ":"
                }).orElse(" ") + Math.min(count, max) + "/" + max
            })
            sum = Math.floor(sum / conditions.length)
            switch (parseInt(questProgressFlag)) {
                case QUEST_PROGRESS_FLAG.HALF:
                    if (sum < 50) sum = 50
                    break
                case QUEST_PROGRESS_FLAG.EIGHTY:
                    if (sum < 80) sum = 80
                    break
            }
            saveQuestRate(questNo, sum / 100)
            return String(sum + "%" + result)
        } else {
            saveQuestRate(questNo, -1)
            return "情報未登録"
        }
    } else {
        saveQuestRate(questNo, -1)
        return null
    }
}
