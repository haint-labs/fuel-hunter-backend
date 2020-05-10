package fuel.hunter.grpc

import fuel.hunter.SnapshotQuery
import fuel.hunter.SnapshotResponse
import fuel.hunter.SnapshotServiceGrpcKt.SnapshotServiceCoroutineImplBase
import fuel.hunter.dao.SnapshotDao
import fuel.hunter.extensions.invoke
import fuel.hunter.extensions.snapshotResponse
import io.grpc.stub.StreamObserver

class SnapshotGrpc(private val snapshotDao: SnapshotDao) : SnapshotServiceCoroutineImplBase() {
    override suspend fun getSnapshots(request: SnapshotQuery): SnapshotResponse {
        val snapshots = snapshotDao.getMatching(request)
        return snapshotResponse {
            addAllSnapshots(snapshots)
        }
    }
}