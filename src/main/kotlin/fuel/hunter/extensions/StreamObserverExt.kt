package fuel.hunter.extensions

import io.grpc.stub.StreamObserver
import kotlinx.coroutines.flow.FlowCollector

inline operator fun <T> StreamObserver<T>.invoke(collect: FlowCollector<T>.() -> Unit) {
    try {
        val collector = object : FlowCollector<T> {
            override suspend fun emit(value: T) = onNext(value)
        }

        collector.collect()
    } catch (e: Throwable) {
        onError(e)
    } finally {
        onCompleted()
    }
}