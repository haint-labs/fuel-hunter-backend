package fuel.hunter.grpc

import fuel.hunter.dao.SnapshotDao
import fuel.hunter.SnapshotQuery
import fuel.hunter.SnapshotResponse
import fuel.hunter.SnapshotServiceGrpc
import fuel.hunter.extensions.invoke
import fuel.hunter.extensions.snapshotResponse
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SnapshotGrpc(
    private val snapshotDao: SnapshotDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : SnapshotServiceGrpc.SnapshotServiceImplBase() {

    override fun getSnapshots(request: SnapshotQuery, observer: StreamObserver<SnapshotResponse>) {
        scope.launch {
            val snapshots = snapshotDao.getMatching(request)
            val response = snapshotResponse {
                addAllSnapshots(snapshots)
            }

            observer { emit(response) }
        }
    }
}