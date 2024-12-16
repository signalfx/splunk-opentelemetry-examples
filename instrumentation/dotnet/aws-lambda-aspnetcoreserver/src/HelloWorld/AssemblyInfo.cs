using Amazon.Lambda.Core;
using Amazon.Lambda.Serialization.SystemTextJson;

// Assembly attribute to specify the serializer
[assembly: LambdaSerializer(typeof(DefaultLambdaJsonSerializer))]
