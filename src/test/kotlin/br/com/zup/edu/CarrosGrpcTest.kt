package br.com.zup.edu
import br.com.zup.edu.carros.Carro
import br.com.zup.edu.carros.CarroRepository
import io.micronaut.test.annotation.TransactionMode
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest(
    /*
    Em cenários mais complexos pode ser que você queira que o Micronaut ñ abra uma transação nem @before, @after ou
    @test. Pq vc provavelmente vai estar lidando com um cenario de multipla concorrencia com threads ou com API'S gRPC.
    API's gRPC abrem uma thread para cada requisição, e se vc lembra bem, o controle de transação do Micronaut é
    amarrado por thread, então a thread do test é diferente da thread de servidor gRPC, então o srvidor gRPC ñ consegue
    participar aqui do nosso controle transacional. O que podemos fazer é desligar aqui o controle transacional e dizer
    que ele é false com 'transactional = false'. Ou seja, ñ vamos mais abrir, commitar ou fazer rollback das transações.
    Ou seja, cada chamada ao repositorio ser´auto-commit por padrao. A principio não deve apresentar muitas mudanças,
    mas dependendo da bateria de testes e do tipo de operação, é bom tomar cuidado.
    */
    rollback = false,
    transactionMode = TransactionMode.SINGLE_TRANSACTION, // default = SEPARATE_TRANSACTION
    transactional = false
)
class CarrosGrpcTest {

    /*
    * Estrategias:
    * louça: sujou, limpou -> @AfterEach
    * louça: limpou, sujou -> @BeforeEach
    * louça: usa louça descartavel -> rollback = true
    * louça: uso a louça, jogo fora, compro nova -> banco em memoria ou recriar o banco a cada teste
    */
    @Inject
    lateinit var repository: CarroRepository

    /*Por padrão o o @BeforeEach e o @AfterEach abrem uma transação cada e elas são comitadas, dessa essa transações são
     separadas da transação do teste, contudo, é possivel configurar para que a transação do @Before ou @After executem
     na mesma transação do teste, para isso utilizamos o TransactionMode - Single Transaction*/
    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    /* Por padrão o Micronaut abre a transação no inicio de cada @Test e encerra a transação no final do método,
     contudo como anotamos com rollback = false, no final a transação é comitada*/
    fun `deve inserir um novo carro`() {

        //cenario
        //repository.deleteAll()


        //ação
        repository.save(Carro(modelo = "Gol", placa = "HPX-1234"))


        //validação
        assertEquals(1, repository.count())


    }// commit

    @Test
    fun `deve encontrar carro por placa`() {
        //cenario
        /*repository.deleteAll() -> quando temos um cenário que se repete em varios testes o recomendado é extrair esse
         método para uma função, como podemos ver na função setup()*/
        repository.save(Carro(modelo = "Palio", placa = "OIP-9876"))

        //ação
        val encontrado = repository.existsByPlaca("OIP-9876")

        //validação
        assertTrue(encontrado)


    }

}
