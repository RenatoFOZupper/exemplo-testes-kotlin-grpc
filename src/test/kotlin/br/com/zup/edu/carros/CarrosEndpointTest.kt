package br.com.zup.edu.carros

import br.com.zup.edu.CarroRequest
import br.com.zup.edu.CarrosGrpcServiceGrpc
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@MicronautTest(transactional = false) //É importante desligar o transactional do MicronautTest, pois ele não participa
                                      // do controle transactional de cada @Test
internal class CarrosEndpointTest(@Inject val grpcClient: CarrosGrpcServiceGrpc.CarrosGrpcServiceBlockingStub,
                                  @Inject val repository: CarroRepository) {

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }



    /*
    * 1. Happy path - ok
    * 2. quando ja existe carro com a placa
    * 3. quando os dados de entrada são invalidos
    */

    @Test
    fun `deve cadastrar um novo carro no banco`() {
        //cenario
        //repository.deleteAll() // auto-commit (ela abre e encerra aqui mesmo)




        //acao
        val request = CarroRequest.newBuilder()
            .setModelo("Gol")
            .setPlaca("HPX-1234")
            .build()

        val response = grpcClient.adicionar(request) //auto-commit (repository.save dentro do método)



        //validacao
        with(response) {
            assertNotNull(id)
            assertTrue(repository.existsById(id)) // efeito colateral -> sempre que temos uma acao no DB, é importante
                                                  // verificar esse efeito colateral
        }


    }

    @Test
    fun `nao deve adicionar novo carro quando carro com placa ja existente `() {
        //cenario
        //repository.deleteAll()
        val existente = repository.save(Carro(modelo = "Palio", placa = "OIP-9876"))


        //acao

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.adicionar(CarroRequest.newBuilder()
                .setModelo("Ferrari")
                .setPlaca(existente.placa)
                .build())
        }



        //validacao
        with(error) {
            assertEquals(Status.ALREADY_EXISTS.code, this.status.code)
            assertEquals("carro com placa existente", this.status.description)
        }

    }

    @Test
    fun`nao deve adicionar novo carro quando dados de entrada forem invalidos`() {
        //cenario
        //repository.deleteAll()


        //acao
        val dadosInvalidos = assertThrows<StatusRuntimeException> {
            grpcClient.adicionar(CarroRequest.newBuilder()
                .setModelo("")
                .setPlaca("")
                .build())
        }


        //validacao
        with(dadosInvalidos) {
            assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
            assertEquals("dados de entrada inválidos", status.description)
        }


    }




    @Factory
    class Clients {

        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): CarrosGrpcServiceGrpc.CarrosGrpcServiceBlockingStub {
            return CarrosGrpcServiceGrpc.newBlockingStub(channel)
        }

    }


}

