package example.carnival.micronaut



import javax.inject.Inject
import javax.inject.Named
import javax.validation.constraints.Size
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import java.util.concurrent.Callable
import groovy.util.logging.Slf4j
import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.time.TimeCategory

import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.disposables.Disposable
import io.reactivex.Flowable

import io.micronaut.core.annotation.Nullable
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.Produces
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Body
import io.micronaut.http.MediaType
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.HttpHeaders
import io.micronaut.http.annotation.RequestAttribute
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.annotation.PathVariable

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import org.apache.tinkerpop.gremlin.process.traversal.Traversal
import org.apache.tinkerpop.gremlin.process.traversal.P
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph

import org.apache.tinkerpop.gremlin.structure.Vertex
import org.apache.tinkerpop.gremlin.structure.Edge
import org.apache.tinkerpop.gremlin.structure.Graph
import org.apache.tinkerpop.gremlin.structure.T

import carnival.core.graph.Core
import example.carnival.micronaut.config.AppConfig
import example.carnival.micronaut.graph.CarnivalGraph
import example.carnival.micronaut.GraphModel



@Controller("/")
@Slf4j 
class AppWs {

    ///////////////////////////////////////////////////////////////////////////
    // FIELDS
    ///////////////////////////////////////////////////////////////////////////

    @Inject AppConfig config
    @Inject CarnivalGraph carnivalGraph
    


    ///////////////////////////////////////////////////////////////////////////
    // WEB SERVICE METHODS
    ///////////////////////////////////////////////////////////////////////////


    @Get("/")
    @Produces(MediaType.TEXT_PLAIN)
    String home() {
        log.trace "home"   

        int numVertices
        int numEdges
        carnivalGraph.coreGraph.withTraversal { Graph graph, GraphTraversalSource g ->
            numVertices = g.V().count().next()
            numEdges = g.V().count().next()
        }     

        return """\
Carnival Micronaut Example Server

Config:
${config.name}

Graph:
numVertices: ${numVertices}
numEdges: ${numEdges}
"""
    }

    class Patient {
        String id = ""
        String first_name = ""
        String last_name = ""
    }
    class PatientResponse {
        List<Patient> patients = []
    }

    @Get("/patients")
    @Produces(MediaType.APPLICATION_JSON)
    PatientResponse patients() {
        def response = new PatientResponse()
        carnivalGraph.coreGraph.withTraversal { Graph graph, GraphTraversalSource g ->
            def patientVs = g.V()
                .isa(GraphModel.VX.RESEARCH_ANSWER).as('anw')
                .out(GraphModel.EX.CONTAINS)
                .isa(GraphModel.VX.PATIENT).as('p')
                .select('p')
                .each { m ->
                    
                    Patient p = new Patient()
                    p.id = GraphModel.PX.ID.valueOf(m)
                    p.first_name = GraphModel.PX_PATIENT.FIRST_NAME.valueOf(m)
                    p.last_name = GraphModel.PX_PATIENT.LAST_NAME.valueOf(m)
                    
                    response.patients << p   
                    //response += "\n ${p.id}" 
                }
        }
        response
    }
}