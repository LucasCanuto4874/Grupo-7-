var express = require("express");
var router = express.Router();

var servidorController = require("../controllers/servidorController");

//Recebendo os dados do html e direcionando para a função cadastrar de usuarioController.js
router.post("/cadastrar", function (req, res) {
    servidorController.cadastrar(req, res);
})

router.get("/buscar", function (req, res) {
    servidorController.buscarServidores(req, res);
});

router.put("/editar/:idServidor", function (req, res) {
    servidorController.editarServidor(req, res);
});

router.delete("/deletar/:idServidor", function (req, res) {
    servidorController.excluirServidor(req, res);
});

module.exports = router;