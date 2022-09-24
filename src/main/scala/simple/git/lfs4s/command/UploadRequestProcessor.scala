package simple.git.lfs4s.command

import cats.effect.IO
import cats.implicits._
import simple.git.lfs4s.model._
import simple.git.lfs4s.service.S3PresignedURLIssueService

object UploadRequestProcessor {
  def execute(
      service: S3PresignedURLIssueService,
      request: GitLFSRequest
  ): IO[GitLFSResponse] = {

    request.objects
      .map(o => {
        service
          .issueUploadPresignedRequestURL(key = o.oid)
          .map(url =>
            GitLFSResponseObject(
              oid = o.oid,
              size = o.size,
              authenticated = false,
              actions =
                Actions(upload = Some(Href(url.url.toString)), download = None),
              expiresAt = url.expireAt
            )
          )
      })
      .sequence
      .map(objects =>
        GitLFSResponse(
          transfer = request.transfers.headOption.getOrElse("basic"),
          objects = objects,
          hashAlgo = request.hashAlgo
        )
      )
  }
}