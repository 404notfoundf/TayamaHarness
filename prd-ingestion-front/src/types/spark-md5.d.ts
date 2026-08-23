declare module 'spark-md5' {
  const SparkMD5: {
    ArrayBuffer: new () => {
      append(data: ArrayBuffer | Uint8Array): void
      end(): string
    }
  }
  export default SparkMD5
}