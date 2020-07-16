package martinek.segasesu.brain

/**
 * Created by Kexik on 17.04.2017.
 */

/*
 * Classes for evaluation requests for binding server data by retrofit
 */
class EvaluationRequest
{
    lateinit var rows : List<EvaluationRow>
}

class EvaluationRow
{
    var id: Int = 0
    var text: String = ""
}

class EvaluationResponse(val list: List<EvaluationResponseRow>)

class EvaluationResponseRow(val id: Int, val res: Int)
