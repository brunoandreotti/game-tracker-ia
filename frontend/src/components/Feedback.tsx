import { useState } from 'react'

interface MessageProps {
  message: string
}

export function LoadingMessage({ message }: MessageProps) {
  return <p className="feedback-loading">{message}</p>
}

export function ErrorMessage({ message }: MessageProps) {
  return <p className="feedback-error" role="alert">{message}</p>
}

interface CoverImageProps {
  src: string | null
  alt: string
  width?: number
  height?: number
  className?: string
}

export function CoverImage({ src, alt, width = 48, height = 64, className }: CoverImageProps) {
  const [hidden, setHidden] = useState(false)

  if (!src || hidden) {
    return null
  }

  return (
    <img
      src={src}
      alt={alt}
      width={width}
      height={height}
      className={className ?? 'cover-image'}
      onError={() => setHidden(true)}
    />
  )
}
