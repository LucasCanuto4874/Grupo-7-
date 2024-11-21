package dominio

import com.github.britooo.looca.api.core.Looca
import oshi.SystemInfo
import repositorio.DadosRepositorio

class Dados {
    var id: Int = 0
    var totalDadosEnviados = 0.0
    var totalDadosRecebidos = 0.0
    var totalPacotesEnviados = 0
    var totalPacotesRecebidos = 0
    var limiteRede: Double = 0.0
    var limiteCpu: Double = 0.0
    var limiteRam: Double = 0.0
    var limiteDisco: Double = 0.0
    var qtdServicosAtivos: Int = 0
    var cargaSistema: Int = 0
    var nThreads: Int = 0

    val looca = Looca()
    val oshi = SystemInfo()
    var dadosRepositorio = DadosRepositorio()

    private var capturando = false

    data class DadosDeRede(val enviados: Double, val recebidos: Double)

    fun converterParaMb(totalDados: Double): Double {
        return totalDados / (1024.0 * 1024.0)
    }

    fun capturarDados() {
        Thread {
            capturando = true

            var totalRam = looca.memoria.total / (1024 * 1024 * 1024)
            println(totalRam)
            inserirTotalRam(totalRam)

            while (capturando) {
                val interfacesDeRede = looca.rede.grupoDeInterfaces.interfaces
                interfacesDeRede.forEach { interfaceDeRede ->
                    totalDadosEnviados += interfaceDeRede.bytesEnviados.toDouble()
                    totalDadosRecebidos += interfaceDeRede.bytesRecebidos.toDouble()
                    totalPacotesEnviados += interfaceDeRede.pacotesEnviados.toInt()
                    totalPacotesRecebidos += interfaceDeRede.pacotesRecebidos.toInt()
                }

                qtdServicosAtivos += looca.grupoDeServicos.servicosAtivos.size
                inserirServicos(qtdServicosAtivos)

                cargaSistema += looca.grupoDeProcessos.processos.size
                inserirCargaSistema(cargaSistema)

                nThreads += looca.grupoDeProcessos.totalThreads
                inserirThreads(nThreads)

                var totalSwap = oshi.hardware.memory.virtualMemory.swapTotal / (1024 * 1024 * 1024)
                println(totalSwap)
                if (totalSwap != null){
                    inserirTotalSwap(totalSwap)
                }else{
                    inserirTotalSwap(0)
                }

                val totalDadosRecebidosMB = converterParaMb(totalDadosRecebidos)
                inserirDados(totalDadosRecebidosMB)

                exibirDados()

                //alertar(limiteRede)

                Thread.sleep(5000)
            }
        }.start()
    }

    fun inserirDados(totalDadosRecebidosMB: Double) {
        dadosRepositorio.inserir(totalDadosRecebidosMB)
    }

    fun inserirServicos(qtdServico: Int){
        dadosRepositorio.inserirServicos(qtdServico)
    }

    fun inserirCargaSistema(cargaSistema: Int){
        dadosRepositorio.inserirCargaSistema(cargaSistema)
    }

    fun inserirThreads(nThread: Int){
        dadosRepositorio.inserirThreads(nThread)
    }

    fun inserirTotalRam(totalRam: Long){
        dadosRepositorio.inserirTotalRam(totalRam)
    }

    fun inserirTotalSwap(totalSwap: Long){
        dadosRepositorio.inserirTotalSwap(totalSwap)
    }

    fun exibirDados() {
        val dadosRede = DadosDeRede(
            enviados = converterParaMb(totalDadosEnviados),
            recebidos = converterParaMb(totalDadosRecebidos)
        )
        println("Total de Dados Enviados: %.2f MB".format(dadosRede.enviados))
        println("Total de Dados Recebidos: %.2f MB".format(dadosRede.recebidos))
        println("Total de Pacotes Enviados: $totalPacotesEnviados")
        println("Total de Pacotes Recebidos: $totalPacotesRecebidos")

    }

    fun iniciarCaptura() {
        if (!capturando) {
            capturarDados()
        }
    }
    fun pararCaptura() {
        capturando = false
    }

    /*fun alertar(alertaUsuario: Double) {
        if (totalDadosRecebidos >= alertaUsuario) {
            val slack = Slack("https://hooks.slack.com/services/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")
            val mensagem = JSONObject().apply {
                put("text", "O servidor ${dadosRepositorio.buscarServidor()} está em estado de alerta, por favor resolva o quanto antes.");
            }
            slack.sendMessage(mensagem);
            println("Alerta enviado: Dados recebidos ultrapassaram $alertaUsuario MB.")

            totalDadosRecebidos = 0.0
        } else {
            println("Estável")
        }
    }*/
}
